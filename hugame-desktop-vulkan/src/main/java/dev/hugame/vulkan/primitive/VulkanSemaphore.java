package dev.hugame.vulkan.primitive;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanSemaphore {
	public static VulkanSemaphore create(VulkanGraphics graphics) {
		try (var memoryStack = stackPush()) {
			var semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

			var semaphoreHandleBuffer = memoryStack.callocLong(1);

			if (vkCreateSemaphore(graphics.getDevice().getLogical(), semaphoreCreateInfo, null, semaphoreHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to create Vulkan semaphore");
			}

			return new VulkanSemaphore(semaphoreHandleBuffer.get(0));
		}
	}

	private final long handle;

	private VulkanSemaphore(long handle) {
		this.handle = handle;
	}

	public long getHandle() {
		return handle;
	}
}
