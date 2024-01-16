package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.vulkan.VkCommandBuffer;

public class BindVertexBuffersCommand extends VulkanCommand {
  private final List<VulkanVertexBuffer> vertexBuffers;

  public BindVertexBuffersCommand(VulkanVertexBuffer... vertexBuffers) {
    this.vertexBuffers = Arrays.asList(vertexBuffers);
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var vertexBufferHandleBuffer = memoryStack.mallocLong(vertexBuffers.size());
      for (var vertexBuffer : vertexBuffers) {
        vertexBufferHandleBuffer.put(vertexBuffer.getHandle());
      }
      vertexBufferHandleBuffer.rewind();

      var offsetBuffer = memoryStack.longs(0, 0);

      vkCmdBindVertexBuffers(commandBuffer, 0, vertexBufferHandleBuffer, offsetBuffer);
    }
  }
}
