package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BindVertexBufferCommand extends VulkanCommand {
	private final List<VulkanVertexBuffer> vertexBuffers;

	public BindVertexBufferCommand(List<VulkanVertexBuffer> vertexBuffers) {
		this.vertexBuffers = vertexBuffers;
	}

	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		try (var memoryStack = stackPush()) {
			var vertexBufferHandleBuffer = memoryStack.mallocLong(vertexBuffers.size());
			for (var vertexBuffer : vertexBuffers) {
				vertexBufferHandleBuffer.put(vertexBuffer.getHandle());
			}
			vertexBufferHandleBuffer.rewind();

			var offsetBuffer = memoryStack.longs(0);

			vkCmdBindVertexBuffers(commandBuffer, 0, vertexBufferHandleBuffer, offsetBuffer);
		}
	}
}
