package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.graphics.FrameBuffer;
import dev.hugame.vulkan.image.DepthBuffer;
import dev.hugame.vulkan.image.VulkanImage;
import dev.hugame.vulkan.image.VulkanImageView;
import dev.hugame.vulkan.pipeline.VulkanPipeline;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VulkanCustomFrameBuffer implements FrameBuffer {
  public static VulkanCustomFrameBuffer create(
      VulkanGraphics graphics,
      VulkanPipeline pipeline,
      VulkanImage colorImage,
      VulkanImageView colorImageView,
      DepthBuffer depthBuffer,
      int width,
      int height) {
    try (var memoryStack = stackPush()) {
      var frameBufferCreateInfo =
          VkFramebufferCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
              .renderPass(pipeline.getRenderPass().getHandle())
              .pAttachments(
                  memoryStack.longs(
                      colorImageView.getHandle(), depthBuffer.getImageView().getHandle()))
              .width(width)
              .height(height)
              .layers(1);

      var frameBufferHandleBuffer = memoryStack.callocLong(1);
      if (vkCreateFramebuffer(
              graphics.getDevice().getLogical(),
              frameBufferCreateInfo,
              null,
              frameBufferHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create frame buffer");
      }

      var frameBufferHandle = frameBufferHandleBuffer.get(0);

      return new VulkanCustomFrameBuffer(
          frameBufferHandle, colorImage, colorImageView, width, height);
    }
  }

  @Getter private final long handle;
  @Getter private final VulkanImage image;
  @Getter private final VulkanImageView imageView;
  @Getter private final int width;
  @Getter private final int height;
}
