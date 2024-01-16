package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;

public class EndRenderPassCommand extends VulkanCommand {
  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    vkCmdEndRenderPass(commandBuffer);
  }
}
