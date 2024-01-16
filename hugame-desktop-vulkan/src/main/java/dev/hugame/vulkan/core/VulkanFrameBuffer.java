package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

public class VulkanFrameBuffer {
  // TODO: Make this be done by the swap chain, passing the swap chain's only depth buffer
  public static List<VulkanFrameBuffer> createAll(VulkanGraphics graphics) {
    var swapChain = graphics.getSwapChain();
    var swapChainImageViewHandles = swapChain.getImageViewHandles();
    var renderPass = graphics.getModelPipeline().getRenderPass();
    var swapChainExtent = swapChain.getExtent();
    var logicalDevice = graphics.getDevice().getLogical();
    var depthBuffer = swapChain.getDepthBuffer();
    var depthBufferImageView = depthBuffer.getImageView();

    try (var memoryStack = stackPush()) {
      var frameBuffers = new ArrayList<VulkanFrameBuffer>();

      for (long imageViewHandle : swapChainImageViewHandles) {
        var frameBufferCreateInfo =
            VkFramebufferCreateInfo.calloc(memoryStack)
                .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .renderPass(renderPass.getHandle())
                .pAttachments(memoryStack.longs(imageViewHandle, depthBufferImageView.getHandle()))
                .width(swapChainExtent.width())
                .height(swapChainExtent.height())
                .layers(1);

        var frameBufferHandleBuffer = memoryStack.callocLong(1);
        if (vkCreateFramebuffer(logicalDevice, frameBufferCreateInfo, null, frameBufferHandleBuffer)
            != VK_SUCCESS) {
          throw new RuntimeException("[HuGame] Failed to create frame buffer");
        }

        var frameBufferHandle = frameBufferHandleBuffer.get(0);
        frameBuffers.add(new VulkanFrameBuffer(frameBufferHandle));
      }

      return frameBuffers;
    }
  }

  private final long handle;

  private VulkanFrameBuffer(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
