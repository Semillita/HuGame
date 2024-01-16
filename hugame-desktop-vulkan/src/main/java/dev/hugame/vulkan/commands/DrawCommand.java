package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;

// TODO: Can later split this class into e.g. InstancedDrawCommand and BatchedDrawCommand.
public class DrawCommand extends VulkanCommand {
  private final int indexCount;
  private final int instanceCount;

  public DrawCommand(int indexCount, int instanceCount) {
    this.indexCount = indexCount;
    this.instanceCount = instanceCount;
  }

  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    vkCmdDrawIndexed(commandBuffer, indexCount, instanceCount, 0, 0, 0);
  }
}
