package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.graphics.FrameBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VulkanSwapChainFrameBuffer implements FrameBuffer {
  // TODO: Make this be done by the swap chain, passing the swap chain's only depth buffer
  public static List<VulkanSwapChainFrameBuffer> createAll(VulkanGraphics graphics) {
    var swapChain = graphics.getSwapChain();
    var swapChainImageViewHandles = swapChain.getImageViewHandles();
    // TODO: Look into this: Using the model pipeline's render pass for swap chain frame buffers
    // isn't necessarily correct.
    var renderPass = graphics.getModelPipeline().getPipeline().getRenderPass();
    var swapChainExtent = swapChain.getExtent();
    var width = swapChainExtent.width();
    var height = swapChainExtent.height();
    var logicalDevice = graphics.getDevice().getLogical();
    var depthBuffer = swapChain.getDepthBuffer();
    var depthBufferImageView = depthBuffer.getImageView();

    try (var memoryStack = stackPush()) {
      var frameBuffers = new ArrayList<VulkanSwapChainFrameBuffer>();

      for (long imageViewHandle : swapChainImageViewHandles) {
        var frameBufferCreateInfo =
            VkFramebufferCreateInfo.calloc(memoryStack)
                .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .renderPass(renderPass.getHandle())
                .pAttachments(memoryStack.longs(imageViewHandle, depthBufferImageView.getHandle()))
                .width(width)
                .height(height)
                .layers(1);

        var frameBufferHandleBuffer = memoryStack.callocLong(1);
        if (vkCreateFramebuffer(logicalDevice, frameBufferCreateInfo, null, frameBufferHandleBuffer)
            != VK_SUCCESS) {
          throw new RuntimeException("[HuGame] Failed to create frame buffer");
        }

        var frameBufferHandle = frameBufferHandleBuffer.get(0);
        frameBuffers.add(new VulkanSwapChainFrameBuffer(frameBufferHandle, width, height));
      }

      return frameBuffers;
    }
  }

  private final long handle;
  private final int width;
  private final int height;

  public long getHandle() {
    return handle;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }
}
