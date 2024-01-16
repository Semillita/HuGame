package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;

public class PersistentCommandExecutor implements CommandExecutor {
	private final VulkanGraphics graphics;
	private final VulkanCommandBuffer commandBuffer;

	public PersistentCommandExecutor(VulkanGraphics graphics) {
		this.graphics = graphics;
		this.commandBuffer = VulkanCommandBuffer.create(graphics);
	}

	@Override
	public void execute(VulkanCommand... commands) {
		commandBuffer.reset();
		commandBuffer.record(graphics, commands);

		graphics.getDevice().submit(commandBuffer);
	}
}