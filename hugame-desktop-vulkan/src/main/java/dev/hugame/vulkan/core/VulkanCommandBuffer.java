package dev.hugame.vulkan.core;

import dev.hugame.vulkan.commands.VulkanCommand;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanCommandBuffer {
	// TODO: Move this creation into VulkanCommandPool
	public static VulkanCommandBuffer create(VulkanGraphics graphics) {
		var device = graphics.getDevice();
		var logicalDevice = device.getLogical();
		try (var memoryStack = stackPush()) {
			var commandBufferAllocationInfo = VkCommandBufferAllocateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.commandPool(graphics.getCommandPool().getHandle())
					.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandBufferCount(1);

			var commandBufferHandleBuffer = memoryStack.callocPointer(1);

			if (vkAllocateCommandBuffers(logicalDevice, commandBufferAllocationInfo, commandBufferHandleBuffer)
					!= VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to allocate command buffer");
			}

			var commandBufferHandle = commandBufferHandleBuffer.get(0);
			var vkCommandBuffer = new VkCommandBuffer(commandBufferHandle, logicalDevice);

			return new VulkanCommandBuffer(vkCommandBuffer);
		}
	}

	private final VkCommandBuffer vkCommandBuffer;

	private VulkanCommandBuffer(VkCommandBuffer vkCommandBuffer) {
		this.vkCommandBuffer = vkCommandBuffer;
	}

	public VkCommandBuffer getVkCommandBuffer() {
		return vkCommandBuffer;
	}

	public void record(VulkanGraphics graphics, VulkanCommand... commands) {
		try (var memoryStack = stackPush()) {
			var commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
					.flags(0)
					.pInheritanceInfo(null);

			if (vkBeginCommandBuffer(vkCommandBuffer, commandBufferBeginInfo) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to begin command buffer");
			}

			for (var command : commands) {
				command.record(vkCommandBuffer, graphics);
			}

			if (vkEndCommandBuffer(vkCommandBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to record command buffer");
			}
		}
	}

	public void reset() {
		vkResetCommandBuffer(vkCommandBuffer, 0);
	}

	// TODO: Make it so that this one doesn't require VulkanGraphics instance.
	public void free(VulkanGraphics graphics) {
		vkFreeCommandBuffers(graphics.getDevice().getLogical(), graphics.getCommandPool().getHandle(), vkCommandBuffer);
	}
}
