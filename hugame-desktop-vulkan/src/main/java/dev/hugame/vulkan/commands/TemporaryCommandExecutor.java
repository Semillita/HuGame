package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;

public class TemporaryCommandExecutor implements CommandExecutor {
	private final VulkanGraphics graphics;

	public TemporaryCommandExecutor(VulkanGraphics graphics) {
		this.graphics = graphics;
		// TODO: Also create quick command pool
	}

	@Override
	public void execute(VulkanCommand... commands) {
		// TODO: Create from command pool unique to the command executor
		var commandBuffer = VulkanCommandBuffer.create(graphics);
		commandBuffer.record(graphics, commands);

		graphics.getDevice().submit(commandBuffer);
		commandBuffer.free(graphics);
	}
}
