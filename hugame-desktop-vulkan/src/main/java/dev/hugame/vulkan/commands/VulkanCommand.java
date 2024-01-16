package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;

public abstract class VulkanCommand {
  public abstract void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics);
}
