package dev.hugame.vulkan.image;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.types.ImageViewType;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class VulkanImageView {
  public static VulkanImageView create(
      VulkanGraphics graphics,
      VulkanImage image,
      ImageViewType imageViewType,
      int format,
      int aspectMask) {
    return create(graphics, image, imageViewType, 1, format, aspectMask);
  }

  public static VulkanImageView create(
      VulkanGraphics graphics,
      VulkanImage image,
      ImageViewType imageViewType,
      int layerCount,
      int format,
      int aspectMask) {
    try (var memoryStack = stackPush()) {
      var imageViewCreateInfo =
          VkImageViewCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
              .image(image.getHandle())
              .viewType(imageViewType.getValue())
              .format(format)
              .subresourceRange(
                  VkImageSubresourceRange.calloc(memoryStack)
                      .aspectMask(aspectMask)
                      .baseMipLevel(0)
                      .levelCount(1)
                      .baseArrayLayer(0)
                      .layerCount(layerCount));

      var imageViewHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateImageView(
              graphics.getDevice().getLogical(), imageViewCreateInfo, null, imageViewHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create image view");
      }

      var imageViewHandle = imageViewHandleBuffer.get(0);

      return new VulkanImageView(imageViewHandle);
    }
  }

  private final long handle;

  private VulkanImageView(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
