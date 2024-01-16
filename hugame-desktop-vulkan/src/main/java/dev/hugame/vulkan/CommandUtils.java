package dev.hugame.vulkan;

import dev.hugame.vulkan.core.VulkanSwapChain;
import org.lwjgl.vulkan.VkViewport;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class CommandUtils {
	public static void setViewport(VulkanSwapChain swapChain) {
		var swapChainExtent = swapChain.getExtent();

		try (var memoryStack = stackPush()) {
			var viewport = VkViewport.calloc(memoryStack)
					.x(0)
					.y(0)
					.width(swapChainExtent.width())
					.height(swapChainExtent.height())
					.minDepth(0)
					.maxDepth(1);

		}
	}
}
