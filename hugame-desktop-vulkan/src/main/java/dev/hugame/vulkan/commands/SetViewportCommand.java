package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class SetViewportCommand extends VulkanCommand {
	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		var swapChainExtent = graphics.getSwapChain().getExtent();

		try (var memoryStack = stackPush()) {
			var viewportBuffer = VkViewport.calloc(1, memoryStack);

			viewportBuffer.get(0)
					.x(0)
					.y(0)
					.width(swapChainExtent.width())
					.height(swapChainExtent.height())
					.minDepth(0)
					.maxDepth(1);

			vkCmdSetViewport(commandBuffer, 0, viewportBuffer);
		}
	}
}
