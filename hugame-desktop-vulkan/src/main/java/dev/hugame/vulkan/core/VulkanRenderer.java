package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK12.*;

import dev.hugame.environment.Environment;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.RenderTarget;
import dev.hugame.graphics.Renderer;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.graphics.text.Font;
import dev.hugame.util.Logger;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.commands.*;
import dev.hugame.vulkan.image.ImageUtils;
import dev.hugame.vulkan.renderer.ModelRenderer;
import dev.hugame.vulkan.renderer.QuadRenderer;
import dev.hugame.vulkan.sync.*;
import dev.hugame.vulkan.text.TextRenderer;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPresentInfoKHR;

public class VulkanRenderer implements Renderer {
  private static final long UNSIGNED_LONG_MAX_VALUE = 0xFFFFFFFFFFFFFFFFL;

  private final VulkanGraphics graphics;

  @Getter private final SetViewportCommand setViewportCommand;
  @Getter private final SetScissorCommand setScissorCommand;
  @Getter private final EndRenderPassCommand endRenderPassCommand;

  private final SyncManager syncManager;

  private final List<UsedCommandBuffer> usedCommandBuffers = new ArrayList<>();

  private final ModelRenderer modelRenderer;
  private final QuadRenderer quadRenderer;
  private final TextRenderer textRenderer;

  @Getter private int currentImageIndex;
  @Getter @Setter private boolean hasDrawnDuringCurrentFrame = false;
  @Getter int frame = 0;

  VulkanRenderer(VulkanGraphics graphics) {
    this.graphics = graphics;

    this.setViewportCommand = new SetViewportCommand();
    this.setScissorCommand = new SetScissorCommand();
    this.endRenderPassCommand = new EndRenderPassCommand();

    this.syncManager = new SyncManager();

    this.modelRenderer = new ModelRenderer(graphics);
    this.quadRenderer = new QuadRenderer(graphics);
    this.textRenderer = new TextRenderer(graphics);
  }

  @Override
  public void create() {
    Logger.pushScope("VulkanRenderer#create");
    modelRenderer.setMaterials(Materials.collect());
    Logger.popScope();
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
    modelRenderer.draw(model, transform);
  }

  @Override
  public void drawText(String text, Font font, int fontSize, int x, int y) {
    textRenderer.drawText(graphics, text, font, fontSize, x, y);
  }

  @Override
  public void flushTextRenderer() {
    textRenderer.flushTextRenderer(graphics);
  }

  @Override
  public void setRenderTarget(RenderTarget renderTarget) {}

  @Override
  public void flush() {
    modelRenderer.flush(graphics);
  }

  public void renderBatch(VulkanBatch batch) {
    quadRenderer.renderBatch(graphics, batch);
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
    // return camera;
    return modelRenderer.getCamera();
  }

  @Override
  public void updateEnvironment(Environment environment) {
    modelRenderer.updateEnvironment(environment);
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

  public List<VulkanCommand> getClearBufferCommands() {
    var swapChain = graphics.getSwapChain();
    var swapChainColorImageHandle = swapChain.getImageHandles().get(currentImageIndex);
    var depthBufferImage = swapChain.getDepthBuffer().getImage();

    return List.of(
        new PipelineBarrierCommand(
            swapChainColorImageHandle,
            VK_IMAGE_LAYOUT_UNDEFINED,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_ASPECT_COLOR_BIT,
            0,
            1),
        new PipelineBarrierCommand(
            depthBufferImage.getHandle(),
            VK_IMAGE_LAYOUT_UNDEFINED,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_ASPECT_DEPTH_BIT,
            0,
            1),
        new ClearColorImageCommand(swapChainColorImageHandle, graphics.getClearColor()),
        new ClearDepthStencilImageCommand(depthBufferImage.getHandle()),
        new PipelineBarrierCommand(
            swapChainColorImageHandle,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
            VK_IMAGE_ASPECT_COLOR_BIT,
            0,
            1),
        new PipelineBarrierCommand(
            depthBufferImage.getHandle(),
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
            VK_IMAGE_ASPECT_DEPTH_BIT,
            0,
            1));
  }

  private record UsedCommandBuffer(VulkanCommandBuffer commandBuffer, int frame) {}
}
