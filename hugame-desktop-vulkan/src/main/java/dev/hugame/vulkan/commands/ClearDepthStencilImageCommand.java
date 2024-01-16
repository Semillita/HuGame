package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageSubresourceRange;

public class ClearDepthStencilImageCommand extends VulkanCommand {
  private final long imageHandle;

  public ClearDepthStencilImageCommand(long imageHandle) {
    this.imageHandle = imageHandle;
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = MemoryStack.stackPush()) {
      var clearValue =
          VkClearDepthStencilValue.calloc(memoryStack)
              .set(1f, 0); // Probably not the correct depth value.

      var depthStencilSubresourceRange =
          VkImageSubresourceRange.calloc(memoryStack)
              .aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
              .baseMipLevel(0)
              .levelCount(1)
              .baseArrayLayer(0)
              .layerCount(1);

      vkCmdClearDepthStencilImage(
          commandBuffer,
          imageHandle,
          VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
          clearValue,
          depthStencilSubresourceRange);
    }
  }
}
