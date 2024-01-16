package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK12.*;

import dev.hugame.core.Renderer;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.util.Logger;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanShaderStorageBuffer;
import dev.hugame.vulkan.commands.*;
import dev.hugame.vulkan.image.ImageUtils;
import dev.hugame.vulkan.layout.DescriptorSource;
import dev.hugame.vulkan.model.VulkanModel;
import dev.hugame.vulkan.sync.*;
import java.util.*;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPresentInfoKHR;

public class VulkanRenderer implements Renderer {
  private static final long UNSIGNED_LONG_MAX_VALUE = 0xFFFFFFFFFFFFFFFFL;

  private final VulkanGraphics graphics;
  private PerspectiveCamera camera;

  private final SetViewportCommand setViewportCommand;
  private final SetScissorCommand setScissorCommand;
  private final EndRenderPassCommand endRenderPassCommand;

  private final Map<Model, List<Transform>> modelInstanceData;
  private final SyncManager syncManager;

  private final VulkanShaderStorageBuffer<Material> materialBuffer;
  private final VulkanShaderStorageBuffer<PointLight> pointLightBuffer;
  private final VulkanShaderStorageBuffer<SpotLight> spotLightBuffer;
  private final VulkanShaderStorageBuffer<DirectionalLight> directionalLightBuffer;

  private final List<UsedCommandBuffer> usedCommandBuffers = new ArrayList<>();

  private int currentImageIndex;
  private boolean hasDrawnDuringCurrentFrame = false;
  int frame = 0;

  VulkanRenderer(VulkanGraphics graphics) {
    this.graphics = graphics;

    camera = new PerspectiveCamera(new Vector3f(100, 100, 100));
    camera.lookAt(new Vector3f(0, 0, 0));
    camera.update();

    this.setViewportCommand = new SetViewportCommand();
    this.setScissorCommand = new SetScissorCommand();
    this.endRenderPassCommand = new EndRenderPassCommand();

    this.modelInstanceData = new HashMap<>();
    this.syncManager = new SyncManager();

    this.materialBuffer = VulkanShaderStorageBuffer.create(graphics, Material.SIZE_IN_BYTES, 1);
    this.pointLightBuffer =
        VulkanShaderStorageBuffer.create(graphics, PointLight.SIZE_IN_BYTES, 10);
    this.spotLightBuffer = VulkanShaderStorageBuffer.create(graphics, SpotLight.SIZE_IN_BYTES, 10);
    this.directionalLightBuffer =
        VulkanShaderStorageBuffer.create(graphics, DirectionalLight.SIZE_IN_BYTES, 10);
  }

  @Override
  public void create() {
    var materials = Materials.collect();
    materialBuffer.fill(materials);
  }

  // TODO: Make beginFrame accept an int frame which is kept track of by the engine, not the
  //  graphics implementation.
  @Override
  public void beginFrame() {
    clearOldCommandBuffers();

    var device = graphics.getDevice();
    var logicalDevice = device.getLogical();

    var inFlightFrameIndex = frame % graphics.getFramesInFlightCount();
    var inFlightFrame = graphics.getFramesInFlight().get(inFlightFrameIndex);
    var inFlightFenceHandle = inFlightFrame.getFence().getHandle();

    var imageIndex = acquireNextImage(inFlightFrame);
    if (imageIndex == null) {
      return;
    }

    vkWaitForFences(logicalDevice, inFlightFenceHandle, true, UNSIGNED_LONG_MAX_VALUE);
    vkResetFences(logicalDevice, inFlightFenceHandle);

    currentImageIndex = imageIndex;
    hasDrawnDuringCurrentFrame = false;
  }

  @Override
  public void draw(Model model, Transform transform, Material material) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void draw(Model model, Transform transform) {
    modelInstanceData.computeIfAbsent(model, ignored -> new ArrayList<>()).add(transform);
  }

  @Override
  public void flush() {
    if (modelInstanceData.isEmpty()) {
      return;
    }

    var currentFrameVertexShaderUniformBuffer =
        graphics.getModelPipelineVertexShaderUniformBuffers().get(currentImageIndex);
    currentFrameVertexShaderUniformBuffer.update(
        buffer -> {
          camera.getViewMatrix().get(0, buffer);
          buffer.position(buffer.position() + 16 * Float.BYTES);
          camera.getProjectionMatrix().get(buffer);
        });

    var currentFrameFragmentShaderUniformBuffer =
        graphics.getModelPipelineFragmentShaderUniformBuffers().get(currentImageIndex);
    currentFrameFragmentShaderUniformBuffer.update(
        buffer -> {
          camera.getPosition().get(buffer);
          buffer.position(buffer.position() + 3 * Float.BYTES);
          buffer.putInt(pointLightBuffer.getItemCount());
          buffer.putInt(spotLightBuffer.getItemCount());
          buffer.putInt(directionalLightBuffer.getItemCount());
        });

    for (var modelAndTransforms : modelInstanceData.entrySet()) {
      var uncheckedModel = modelAndTransforms.getKey();
      var instanceTransforms = modelAndTransforms.getValue();
      if (!(uncheckedModel instanceof VulkanModel model)) {
        throw new RuntimeException("Invalid type of Model: " + uncheckedModel.getClass());
      }

      renderModel(model, instanceTransforms);
    }

    modelInstanceData.clear();
  }

  public void renderBatch(VulkanBatch batch) {
    var device = graphics.getDevice();

    var swapChain = graphics.getSwapChain();

    var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);

    var swapChainColorImageHandle = swapChain.getImageHandles().get(currentImageIndex);

    var depthBufferImage = swapChain.getDepthBuffer().getImage();

    var currentFrameUniformBuffer = graphics.getQuadPipelineUniformBuffers().get(currentImageIndex);

    var descriptorSets = graphics.getQuadPipelineDescriptorSets();
    var currentDescriptorSet = descriptorSets.get(currentImageIndex);

    var commandBuffer = graphics.getCommandBuffer();

    var inFlightFrameIndex = frame % graphics.getFramesInFlightCount();
    var inFlightFrame = graphics.getFramesInFlight().get(inFlightFrameIndex);
    var imageAvailableSemaphore = inFlightFrame.getImageAvailableSemaphore();

    var vertexBuffer = batch.getVertexBuffer();
    var indexBuffer = batch.getIndexBuffer();

    BufferUtils.fillWithStagingBuffer(
        graphics, vertexBuffer.getBuffer(), batch.getVertexDataBuffer());

    var textureArrays = batch.getTextureArrays();

    currentDescriptorSet.write(
        graphics,
        DescriptorSource.fromUniformBuffer(currentFrameUniformBuffer),
        DescriptorSource.fromTextureArrays(textureArrays, 32));

    var pipeline = graphics.getQuadPipeline();

    var commands =
        new ArrayList<VulkanCommand>() {
          {
            if (!hasDrawnDuringCurrentFrame) {
              add(
                  new PipelineBarrierCommand(
                      swapChainColorImageHandle,
                      VK_IMAGE_LAYOUT_UNDEFINED,
                      VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                      VK_IMAGE_ASPECT_COLOR_BIT,
                      0,
                      1));
              add(
                  new PipelineBarrierCommand(
                      depthBufferImage.getHandle(),
                      VK_IMAGE_LAYOUT_UNDEFINED,
                      VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                      VK_IMAGE_ASPECT_DEPTH_BIT,
                      0,
                      1));
              add(new ClearColorImageCommand(swapChainColorImageHandle, graphics.getClearColor()));
              add(new ClearDepthStencilImageCommand(depthBufferImage.getHandle()));
            }

            addAll(
                Arrays.asList(
                    new BeginRenderPassCommand(pipeline.getRenderPass(), frameBuffer),
                    new BindPipelineCommand(pipeline.getHandle()),
                    setViewportCommand,
                    setScissorCommand,
                    new BindVertexBuffersCommand(vertexBuffer),
                    new BindIndexBufferCommand(indexBuffer),
                    new BindDescriptorSetsCommand(currentDescriptorSet, pipeline),
                    new DrawCommand(batch.getIndexCount(), 1),
                    endRenderPassCommand));
          }
        };

    commandBuffer.reset();
    commandBuffer.record(graphics, commands);

    var batchCamera = batch.getCamera();
    currentFrameUniformBuffer.update(
        buffer -> {
          batchCamera.getViewMatrix().get(0, buffer);
          batchCamera.getProjectionMatrix().get(16 * Float.BYTES, buffer);
        });

    var waitSyncPoint = hasDrawnDuringCurrentFrame ? null : imageAvailableSemaphore;

    var submitInfo =
        new QueueSubmitInfo()
            .setCommandBuffer(commandBuffer)
            .setWaitSyncPoint(waitSyncPoint)
            .setWaitDestinationStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

    VulkanFence signalFence = null; // Don't use a fence for each draw call

    var submitResult = device.getGraphicsQueue().submit(submitInfo, signalFence);
    if (submitResult != VulkanResult.SUCCESS) {
      throw new RuntimeException("[HuGame] Failed to submit queue");
    }

    hasDrawnDuringCurrentFrame = true;
  }

  @Override
  public void endFrame() {
    var device = graphics.getDevice();
    var swapChain = graphics.getSwapChain();

    var inFlightFrameIndex = frame % graphics.getFramesInFlightCount();
    var inFlightFrame = graphics.getFramesInFlight().get(inFlightFrameIndex);
    var imageAvailableSemaphore = inFlightFrame.getImageAvailableSemaphore();
    var imagePreparedForPresentingSemaphore =
        inFlightFrame.getImagePreparedForPresentingSemaphore();

    try (var memoryStack = stackPush()) {
      var transitionImageLayoutWaitSyncPoint =
          hasDrawnDuringCurrentFrame ? null : imageAvailableSemaphore;

      var currentColorImageLayout =
          hasDrawnDuringCurrentFrame
              ? VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
              : VK_IMAGE_LAYOUT_UNDEFINED;
      var transitionImageLayoutCommandBuffer =
          ImageUtils.transitionImageLayout(
              graphics,
              swapChain.getImageHandles().get(currentImageIndex),
              VK_IMAGE_ASPECT_COLOR_BIT,
              currentColorImageLayout,
              VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
              transitionImageLayoutWaitSyncPoint,
              imagePreparedForPresentingSemaphore);
      usedCommandBuffers.add(new UsedCommandBuffer(transitionImageLayoutCommandBuffer, frame));

      // Make an empty queue submit to adapt to a fence after all other operations.
      var adaptQueueSubmitInfo = new QueueSubmitInfo();
      device.getGraphicsQueue().submit(adaptQueueSubmitInfo, inFlightFrame.getFence());

      var presentInfo =
          VkPresentInfoKHR.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
              .swapchainCount(1)
              .pSwapchains(memoryStack.longs(swapChain.getHandle()))
              .pImageIndices(memoryStack.ints(currentImageIndex))
              .pWaitSemaphores(
                  memoryStack.longs(imagePreparedForPresentingSemaphore.getSemaphoreHandle()));

      var presentResult = device.getPresentQueue().present(presentInfo);

      if (presentResult == VK_ERROR_OUT_OF_DATE_KHR
          || presentResult == VK_SUBOPTIMAL_KHR
          || graphics.frameBufferResized()) {
        graphics.setFrameBufferResized(false);
        graphics.recreateSwapChain();
      } else if (presentResult != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to queue present");
      }
    }
    frame++;
  }

  @Override
  public PerspectiveCamera getCamera() {
    return camera;
  }

  @Override
  public void updateEnvironment(Environment environment) {
    var pointLights = environment.getPointLights();
    var spotLights = environment.getSpotLights();
    var directionalLights = environment.getDirectionalLights();

    pointLightBuffer.refill(pointLights);
    spotLightBuffer.refill(spotLights);
    directionalLightBuffer.refill(directionalLights);
  }

  private void renderModel(VulkanModel model, List<Transform> transforms) {
    var instanceCount = transforms.size();
    var transformSizeBytes = 16 * Float.BYTES;
    var instanceDataBuffer = MemoryUtil.memAlloc(instanceCount * transformSizeBytes).rewind();

    var instanceOffset = 0;
    for (var transform : transforms) {
      var transformationMatrix = transform.getMatrix();
      transformationMatrix.get(instanceOffset * transformSizeBytes, instanceDataBuffer);

      instanceOffset++;
    }

    var instanceBuffer = model.getInstanceBuffer();
    var instanceBufferContent = instanceBuffer.getBuffer();
    BufferUtils.fillWithStagingBuffer(graphics, instanceBufferContent, instanceDataBuffer);
    MemoryUtil.memFree(instanceDataBuffer);

    var indexBuffer = model.getIndexBuffer();

    var textureArrays = model.getTextureArrays();

    var descriptorSets = graphics.getModelPipelineDescriptorSets();
    var currentDescriptorSet = descriptorSets.get(currentImageIndex);

    var currentFrameVertexShaderUniformBuffer =
        graphics.getModelPipelineVertexShaderUniformBuffers().get(currentImageIndex);
    var currentFrameFragmentShaderUniformBuffer =
        graphics.getModelPipelineFragmentShaderUniformBuffers().get(currentImageIndex);

    currentDescriptorSet.write(
        graphics,
        DescriptorSource.fromUniformBuffer(currentFrameVertexShaderUniformBuffer),
        DescriptorSource.fromUniformBuffer(currentFrameFragmentShaderUniformBuffer),
        DescriptorSource.fromTextureArrays(textureArrays, 32),
        DescriptorSource.fromShaderStorageBuffer(materialBuffer),
        DescriptorSource.fromShaderStorageBuffer(pointLightBuffer),
        DescriptorSource.fromShaderStorageBuffer(spotLightBuffer),
        DescriptorSource.fromShaderStorageBuffer(directionalLightBuffer));

    var device = graphics.getDevice();
    var swapChain = graphics.getSwapChain();
    var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);
    var swapChainColorImageHandle = swapChain.getImageHandles().get(currentImageIndex);
    var depthBufferImage = swapChain.getDepthBuffer().getImage();
    var pipeline = graphics.getModelPipeline();

    var commands =
        new ArrayList<VulkanCommand>() {
          {
            if (!hasDrawnDuringCurrentFrame) {
              add(
                  new PipelineBarrierCommand(
                      swapChainColorImageHandle,
                      VK_IMAGE_LAYOUT_UNDEFINED,
                      VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                      VK_IMAGE_ASPECT_COLOR_BIT,
                      0,
                      1));
              add(
                  new PipelineBarrierCommand(
                      depthBufferImage.getHandle(),
                      VK_IMAGE_LAYOUT_UNDEFINED,
                      VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                      VK_IMAGE_ASPECT_DEPTH_BIT,
                      0,
                      1));
              add(new ClearColorImageCommand(swapChainColorImageHandle, graphics.getClearColor()));
              add(new ClearDepthStencilImageCommand(depthBufferImage.getHandle()));
              add(
                  new PipelineBarrierCommand(
                      swapChainColorImageHandle,
                      VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                      VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                      VK_IMAGE_ASPECT_COLOR_BIT,
                      0,
                      1));
              add(
                  new PipelineBarrierCommand(
                      depthBufferImage.getHandle(),
                      VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                      VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                      VK_IMAGE_ASPECT_DEPTH_BIT,
                      0,
                      1));
            }

            addAll(
                Arrays.asList(
                    new BeginRenderPassCommand(pipeline.getRenderPass(), frameBuffer),
                    new BindPipelineCommand(pipeline.getHandle()),
                    setViewportCommand,
                    setScissorCommand,
                    new BindVertexBuffersCommand(model.getVertexBuffer(), instanceBuffer),
                    new BindIndexBufferCommand(indexBuffer),
                    new BindDescriptorSetsCommand(currentDescriptorSet, pipeline),
                    new DrawCommand(model.getIndexCount(), 100),
                    endRenderPassCommand));
          }
        };

    var commandBuffer = graphics.getCommandBuffer();
    commandBuffer.reset();
    commandBuffer.record(graphics, commands);

    var inFlightFrameIndex = frame % graphics.getFramesInFlightCount();
    var inFlightFrame = graphics.getFramesInFlight().get(inFlightFrameIndex);
    var imageAvailableSemaphore = inFlightFrame.getImageAvailableSemaphore();
    var waitSyncPoint = hasDrawnDuringCurrentFrame ? null : imageAvailableSemaphore;

    var submitInfo =
        new QueueSubmitInfo()
            .setCommandBuffer(commandBuffer)
            .setWaitSyncPoint(waitSyncPoint)
            .setWaitDestinationStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

    VulkanFence signalFence = null; // Don't use a fence for each draw call

    var graphicsQueue = device.getGraphicsQueue();
    var submitResult = graphicsQueue.submit(submitInfo, signalFence);
    if (submitResult != VulkanResult.SUCCESS) {
      throw new RuntimeException("[HuGame] Failed to submit queue: " + submitResult);
    }

    hasDrawnDuringCurrentFrame = true;

    transforms.clear();
  }

  private Integer acquireNextImage(InFlightFrame inFlightFrame) {
    var imageIndexBuffer = MemoryUtil.memAllocInt(1);

    var imageAvailableSemaphore = inFlightFrame.getImageAvailableSemaphore();

    var result =
        vkAcquireNextImageKHR(
            graphics.getDevice().getLogical(),
            graphics.getSwapChain().getHandle(),
            UNSIGNED_LONG_MAX_VALUE,
            imageAvailableSemaphore.getHandle(),
            VK_NULL_HANDLE,
            imageIndexBuffer);

    if (result == VK_ERROR_OUT_OF_DATE_KHR || graphics.frameBufferResized()) {
      graphics.setFrameBufferResized(false);
      graphics.recreateSwapChain();

      return null;
    } else if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
      throw new RuntimeException("[HuGame] Failed to acquire next swap chain image");
    }

    var imageIndex = imageIndexBuffer.get(0);
    MemoryUtil.memFree(imageIndexBuffer);

    return imageIndex;
  }

  private void clearOldCommandBuffers() {
    UsedCommandBuffer usedCommandBuffer;
    while (!usedCommandBuffers.isEmpty()
        && frame - (usedCommandBuffer = usedCommandBuffers.get(0)).frame
            >= (graphics.getFramesInFlightCount() + 1)) {
      usedCommandBuffer.commandBuffer.free(graphics);
      usedCommandBuffers.remove(0);
    }
  }

  private record UsedCommandBuffer(VulkanCommandBuffer commandBuffer, int frame) {}
}
