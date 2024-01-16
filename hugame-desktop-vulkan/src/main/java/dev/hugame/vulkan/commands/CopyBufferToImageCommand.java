package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.buffer.VulkanBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.image.VulkanImage;
import org.lwjgl.vulkan.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class CopyBufferToImageCommand extends VulkanCommand {
	private final VulkanBuffer buffer;
	private final VulkanImage image;
	private final int width;
	private final int height;

	public CopyBufferToImageCommand(VulkanBuffer buffer, VulkanImage image, int width, int height) {
		this.buffer = buffer;
		this.image = image;
		this.width = width;
		this.height = height;
	}

	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		try (var memoryStack = stackPush()) {
			var bufferImageCopyBuffer = VkBufferImageCopy.calloc(1, memoryStack);
			bufferImageCopyBuffer.get(0)
					.bufferOffset(0)
					.bufferRowLength(0)
					.bufferImageHeight(0)
					.imageSubresource(VkImageSubresourceLayers.calloc(memoryStack)
							.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
							.mipLevel(0)
							.baseArrayLayer(0)
							.layerCount(1))
					.imageOffset(VkOffset3D.calloc(memoryStack)
							.x(0)
							.y(0)
							.z(0))
					.imageExtent(VkExtent3D.calloc(memoryStack)
							.width(width)
							.height(height)
							.depth(1));

			vkCmdCopyBufferToImage(
					commandBuffer,
					buffer.getHandle(),
					image.getHandle(),
					VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
					bufferImageCopyBuffer);
		}
	}
}
