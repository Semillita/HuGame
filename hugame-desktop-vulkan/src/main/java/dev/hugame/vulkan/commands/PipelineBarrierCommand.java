package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.image.VulkanImage;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class PipelineBarrierCommand extends VulkanCommand {
	private final VulkanImage image;
	private final int oldImageLayout;
	private final int newImageLayout;

	public PipelineBarrierCommand(VulkanImage image, int oldImageLayout, int newImageLayout) {
		this.image = image;
		this.oldImageLayout = oldImageLayout;
		this.newImageLayout = newImageLayout;
	}

	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		try (var memoryStack = stackPush()) {
			int sourceStage, destinationStage, sourceAccessMask, destinationAccessMask;

			if (oldImageLayout == VK_IMAGE_LAYOUT_UNDEFINED && newImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
				sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
				destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
				sourceAccessMask = 0;
				destinationAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
			} else if (oldImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
					&& newImageLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
				sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
				destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
				sourceAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
				destinationAccessMask = VK_ACCESS_SHADER_READ_BIT;
			} else {
				throw new RuntimeException("[HuGame] Unsupported layout transition");
			}

			var imageMemoryBarrierBuffer = VkImageMemoryBarrier.calloc(1, memoryStack);
			imageMemoryBarrierBuffer.get(0)
					.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
					.oldLayout(oldImageLayout)
					.newLayout(newImageLayout)
					.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.image(image.getHandle())
					.subresourceRange(VkImageSubresourceRange.calloc(memoryStack)
							.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
							.baseMipLevel(0)
							.levelCount(1)
							.baseArrayLayer(0)
							.layerCount(1))
					.srcAccessMask(sourceAccessMask)
					.dstAccessMask(destinationAccessMask);

			vkCmdPipelineBarrier(commandBuffer, sourceStage, destinationStage, 0, null, null, imageMemoryBarrierBuffer);
		}
	}
}
