package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;

public class SetScissorCommand extends VulkanCommand {
  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var scissorRectBuffer = VkRect2D.calloc(1, memoryStack);

      scissorRectBuffer
          .get(0)
          .offset(VkOffset2D.calloc(memoryStack).set(0, 0))
          .extent(graphics.getSwapChain().getExtent());

      vkCmdSetScissor(commandBuffer, 0, scissorRectBuffer);
    }
  }
}
