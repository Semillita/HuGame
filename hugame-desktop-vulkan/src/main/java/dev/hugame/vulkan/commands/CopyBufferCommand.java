package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCommandBuffer;

public class CopyBufferCommand extends VulkanCommand {
  private final long sourceBufferHandle;
  private final long destinationBufferHandle;
  private final long size;

  public CopyBufferCommand(long sourceBufferHandle, long destinationBufferHandle, long size) {
    this.sourceBufferHandle = sourceBufferHandle;
    this.destinationBufferHandle = destinationBufferHandle;
    this.size = size;
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var bufferCopyBuffer = VkBufferCopy.calloc(1, memoryStack);
      bufferCopyBuffer.get(0).srcOffset(0).dstOffset(0).size(size);

      vkCmdCopyBuffer(commandBuffer, sourceBufferHandle, destinationBufferHandle, bufferCopyBuffer);
    }
  }
}
