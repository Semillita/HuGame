package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;

public class BindIndexBufferCommand extends VulkanCommand {
  private final VulkanIndexBuffer indexBuffer;

  public BindIndexBufferCommand(VulkanIndexBuffer indexBuffer) {
    this.indexBuffer = indexBuffer;
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    vkCmdBindIndexBuffer(commandBuffer, indexBuffer.getHandle(), 0, VK_INDEX_TYPE_UINT32);
  }
}
