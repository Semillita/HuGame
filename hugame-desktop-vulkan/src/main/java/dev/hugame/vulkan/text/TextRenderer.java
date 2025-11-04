package dev.hugame.vulkan.text;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.text.Font;
import dev.hugame.util.Files;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.commands.ClearColorImageCommand;
import dev.hugame.vulkan.commands.ClearDepthStencilImageCommand;
import dev.hugame.vulkan.commands.PipelineBarrierCommand;
import dev.hugame.vulkan.commands.VulkanCommand;
import dev.hugame.vulkan.core.RenderInfo;
import dev.hugame.vulkan.core.RenderPipeline;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorSource;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import dev.hugame.vulkan.pipeline.VulkanPipeline;
import java.awt.Dimension;
import java.util.List;
import java.util.stream.IntStream;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextRenderer {
  private static final int VERTEX_SHADER_UNIFORM_BUFFER_SIZE = 2 * 16 * Float.BYTES;
  private static final int FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE = Float.BYTES;

  private final VulkanDescriptorSetLayout descriptorSetLayout; // TODO: Make into local variable
  private final VulkanPipeline pipeline;
  private final List<VulkanUniformBuffer>
      vertexShaderUniformBuffers; // TODO: Maybe put into per-frame struct
  private final List<VulkanUniformBuffer> fragmentShaderUniformBuffers;
  private final List<VulkanDescriptorSet> descriptorSets;
  private final TextBatch textBatch;
  private final RenderPipeline renderPipeline;

  public TextRenderer(VulkanGraphics graphics) {
    var descriptors = new DefaultTextPipelineDescriptors();
    var vertexShaderSource = Files.read("/shaders/vulkan_text_vertex_shader.glsl").orElseThrow();
    var fragmentShaderSource =
        Files.read("/shaders/vulkan_text_fragment_shader.glsl").orElseThrow();
    this.descriptorSetLayout = descriptors.createDescriptorSetLayout(graphics);
    this.pipeline =
        VulkanPipeline.create(
            graphics,
            descriptors,
            descriptorSetLayout,
            vertexShaderSource,
            fragmentShaderSource,
            false);

    this.vertexShaderUniformBuffers =
        IntStream.range(0, graphics.getFramesInFlightCount())
            .mapToObj(
                ignored -> VulkanUniformBuffer.create(graphics, VERTEX_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.fragmentShaderUniformBuffers =
        IntStream.range(0, graphics.getFramesInFlightCount())
            .mapToObj(
                ignored ->
                    VulkanUniformBuffer.create(graphics, FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    var descriptorPool = descriptors.createDescriptorPool(graphics);
    this.descriptorSets = descriptorPool.allocateDescriptorSets(graphics, descriptorSetLayout);

    this.textBatch =
        new TextBatch(
            graphics,
            new Camera2D(
                () -> graphics.getSurfaceContext().getSize(),
                new Vector2f(960, 540),
                new Dimension(960, 540)));

    this.renderPipeline = new RenderPipeline(pipeline);
  }

  public void drawText(
      VulkanGraphics graphics, String text, Font font, int fontSize, int x, int y) {
    if (textBatch.isFull()) {
      flushTextRenderer(graphics);
    }

    textBatch.add(text, font, fontSize, x, y);
  }

  public void flushTextRenderer(VulkanGraphics graphics) {
    Logger.pushScope("VulkanRenderer#flushTextRenderer");
    var device = graphics.getDevice();
    var swapChain = graphics.getSwapChain();
    var renderer = graphics.getRenderer();
    var currentImageIndex = renderer.getCurrentImageIndex();
    var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);
    var swapChainColorImageHandle = swapChain.getImageHandles().get(currentImageIndex);
    var depthBufferImage = swapChain.getDepthBuffer().getImage();

    var camera = textBatch.getCamera();
    // 1) Update uniform buffer content
    var currentFrameVertexShaderUniformBuffer =
        graphics.getTextPipelineVertexShaderUniformBuffers().get(currentImageIndex);
    currentFrameVertexShaderUniformBuffer.update(
        buffer -> {
          camera.getViewMatrix().get(0, buffer);
          camera.getProjectionMatrix().get(16 * Float.BYTES, buffer);
        });

    var pxRange = (250f / 32f) * (float) HuGame.MSDF_RANGE;
    Logger.log("Drawing text with pxRange=" + pxRange);

    var currentFrameFragmentShaderUniformBuffer =
        graphics.getTextPipelineFragmentShaderUniformBuffers().get(currentImageIndex);
    currentFrameFragmentShaderUniformBuffer.update(
        buffer -> {
          buffer.putFloat(pxRange); // TODO: Figure out the actual range
        });

    var descriptorSets = graphics.getTextPipelineDescriptorSets();
    var currentDescriptorSet = descriptorSets.get(currentImageIndex);
    var commandBuffer = graphics.getCommandBuffer();

    var inFlightFrameIndex = renderer.getFrame() % graphics.getFramesInFlightCount();
    var inFlightFrame = graphics.getFramesInFlight().get(inFlightFrameIndex);
    var imageAvailableSemaphore = inFlightFrame.getImageAvailableSemaphore();

    var vertexBuffer = textBatch.getVertexBuffer();
    var indexBuffer = textBatch.getIndexBuffer();
    var vertexDataBuffer = textBatch.getVertexDataBuffer();

    BufferUtils.fillWithStagingBuffer(graphics, vertexBuffer.getBuffer(), vertexDataBuffer);

    var textureArrays = textBatch.getTextureArrays();

    currentDescriptorSet.write(
        graphics,
        DescriptorSource.fromUniformBuffer(currentFrameVertexShaderUniformBuffer),
        DescriptorSource.fromUniformBuffer(currentFrameFragmentShaderUniformBuffer),
        DescriptorSource.fromTextureArrays(textureArrays, 32));

    var hasDrawnDuringCurrentFrame = renderer.isHasDrawnDuringCurrentFrame();

    var waitSyncPoint = hasDrawnDuringCurrentFrame ? null : imageAvailableSemaphore;

    var renderInfo =
        RenderInfo.builder()
            .pipeline(renderPipeline)
            .frameBuffer(frameBuffer)
            .vertexBuffers(List.of(vertexBuffer))
            .indexBuffer(indexBuffer)
            .indexCount(textBatch.getIndexCount())
            .descriptorSet(currentDescriptorSet)
            .commandBuffer(commandBuffer)
            .waitSyncPoint(waitSyncPoint)
            .build();

    graphics.render(renderInfo);

    textBatch.clear();

    renderer.setHasDrawnDuringCurrentFrame(true);
    Logger.popScope();
  }

  // TODO: Commands will be handled within VulkanGraphics#render, so eventually move this to a flag
  //  [boolean clearFrameBuffer] or a struct for the clear details in the input to
  // VulkanGraphics#render
  private List<VulkanCommand> getFirstFrameDrawCommands(
      long swapChainColorImageHandle, long depthBufferImageHandle, Vector4f clearColor) {
    return List.of(
        new PipelineBarrierCommand(
            swapChainColorImageHandle,
            VK_IMAGE_LAYOUT_UNDEFINED,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_ASPECT_COLOR_BIT,
            0,
            1),
        new PipelineBarrierCommand(
            depthBufferImageHandle,
            VK_IMAGE_LAYOUT_UNDEFINED,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_ASPECT_DEPTH_BIT,
            0,
            1),
        new ClearColorImageCommand(swapChainColorImageHandle, clearColor),
        new ClearDepthStencilImageCommand(depthBufferImageHandle),
        new PipelineBarrierCommand(
            swapChainColorImageHandle,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
            VK_IMAGE_ASPECT_COLOR_BIT,
            0,
            1),
        new PipelineBarrierCommand(
            depthBufferImageHandle,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
            VK_IMAGE_ASPECT_DEPTH_BIT,
            0,
            1));
  }
}
