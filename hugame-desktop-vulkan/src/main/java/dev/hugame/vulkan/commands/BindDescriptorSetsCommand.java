package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BindDescriptorSetsCommand extends VulkanCommand {
	private final VulkanDescriptorSet descriptorSet;

	public  BindDescriptorSetsCommand(VulkanDescriptorSet descriptorSet) {
		this.descriptorSet = descriptorSet;
	}

	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		try (var memoryStack = stackPush()) {
			vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphics.getPipeline().getLayoutHandle(),
					0, memoryStack.longs(descriptorSet.getHandle()), null);
		}
	}
}
