package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;
import static org.lwjgl.vulkan.VK10.*;

// TODO: Can later split this class into e.g. InstancedDrawCommand and BatchedDrawCommand.
public class DrawCommand extends VulkanCommand {
	private final int indexCount;

	public DrawCommand(int indexCount) {
		this.indexCount = indexCount;
	}

	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		vkCmdDrawIndexed(commandBuffer, indexCount, 1, 0, 0, 0);
	}
}
