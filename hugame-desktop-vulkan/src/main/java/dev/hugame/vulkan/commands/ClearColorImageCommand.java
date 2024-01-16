package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import lombok.RequiredArgsConstructor;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageSubresourceRange;

@RequiredArgsConstructor
public class ClearColorImageCommand extends VulkanCommand {
  private final long imageHandle;
  private final Vector4f color;

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = MemoryStack.stackPush()) {
      var clearValue =
          VkClearColorValue.calloc(memoryStack)
              .float32(memoryStack.floats(color.x, color.y, color.z, color.w));

      var subresourceRange =
          VkImageSubresourceRange.calloc(memoryStack)
              .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
              .baseMipLevel(0)
              .levelCount(1)
              .baseArrayLayer(0)
              .layerCount(1);

      vkCmdClearColorImage(
          commandBuffer,
          imageHandle,
          VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
          clearValue,
          subresourceRange);
    }
  }
}
