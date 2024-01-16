package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkCommandBuffer;
import static org.lwjgl.vulkan.VK10.*;

public class BindPipelineCommand extends VulkanCommand {
	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphics.getPipeline().getHandle());
	}
}
