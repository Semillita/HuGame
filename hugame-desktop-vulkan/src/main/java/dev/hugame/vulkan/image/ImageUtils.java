package dev.hugame.vulkan.image;

import dev.hugame.graphics.ImageData;
import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanBuffer;
import dev.hugame.vulkan.commands.CopyBufferToImageCommand;
import dev.hugame.vulkan.commands.PipelineBarrierCommand;
import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanDevice;
import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;

public class ImageUtils {

	// TODO: Whoever uses this method has to free the memory in imageData
	public static VulkanImage createImage(VulkanGraphics graphics, ImageData imageData) {
		var dataBuffer = imageData.buffer();
		var width = imageData.width();
		var height = imageData.height();

		var stagingBuffer = BufferUtils.createStagingBuffer(
				graphics,
				dataBuffer.capacity(),
				b -> b.put(dataBuffer));

		var image = createImage(graphics, width, height, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);

		transitionImageLayout(
				graphics,
				image,
				VK_FORMAT_R8G8B8A8_SRGB,
				VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

		copyBufferToImage(graphics, stagingBuffer, image, width, height);

		transitionImageLayout(
				graphics,
				image,
				VK_FORMAT_R8G8B8A8_SRGB,
				VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

		return image;
	}

	// TODO: Maybe add memory properties flags parameter
	public static VulkanImage createImage(VulkanGraphics graphics, int width, int height, int format, int usageFlags) {
		var device = graphics.getDevice();
		var logicalDevice = device.getLogical();

		try (var memoryStack = stackPush()) {

			var imageCreateInfo = VkImageCreateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
					.imageType(VK_IMAGE_TYPE_2D)
					.extent(VkExtent3D.calloc(memoryStack)
							.width(width)
							.height(height)
							.depth(1))
					.mipLevels(1)
					.arrayLayers(1)
					.format(format)
					.tiling(VK_IMAGE_TILING_OPTIMAL)
					.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
					.usage(usageFlags)
					.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
					.samples(VK_SAMPLE_COUNT_1_BIT)
					.flags(0);

			var imageHandleBuffer = memoryStack.callocLong(1);

			if (vkCreateImage(logicalDevice, imageCreateInfo, null, imageHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to create image");
			}

			var imageHandle = imageHandleBuffer.get(0);

			var imageMemoryRequirements = VkMemoryRequirements.calloc(memoryStack);
			vkGetImageMemoryRequirements(logicalDevice, imageHandle, imageMemoryRequirements);

			var memoryAllocateInfo = VkMemoryAllocateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
					.allocationSize(imageMemoryRequirements.size())
					.memoryTypeIndex(
							findMemoryType(
									device,
									imageMemoryRequirements.memoryTypeBits(),
									VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

			var imageMemoryHandleBuffer = memoryStack.callocLong(1);

			if (vkAllocateMemory(logicalDevice, memoryAllocateInfo, null, imageMemoryHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to allocate memory for texture buffer");
			}

			var imageMemoryHandle = imageMemoryHandleBuffer.get(0);

			vkBindImageMemory(logicalDevice, imageHandle, imageMemoryHandle, 0);

			return new VulkanImage(imageHandle, imageMemoryHandle);
		}
	}

	public static void transitionImageLayout(
			VulkanGraphics graphics,
			VulkanImage image,
			int format, // TODO: Use
			int oldImageLayout,
			int newImageLayout) {
		var commandBuffer = VulkanCommandBuffer.create(graphics);

		var pipelineBarrierCommand = new PipelineBarrierCommand(image, oldImageLayout, newImageLayout);
		commandBuffer.record(graphics, pipelineBarrierCommand);

		graphics.getDevice().submit(commandBuffer);
		commandBuffer.free(graphics);
	}

	private static void copyBufferToImage(VulkanGraphics graphics, VulkanBuffer buffer, VulkanImage image, int width, int height) {
		var commandBuffer = VulkanCommandBuffer.create(graphics);

		var copyBufferToImageCommand = new CopyBufferToImageCommand(buffer, image, width, height);
		commandBuffer.record(graphics, copyBufferToImageCommand);

		graphics.getDevice().submit(commandBuffer);
		commandBuffer.free(graphics);
	}

	private static int findMemoryType(VulkanDevice device, int typeFilter, int properties) {
		try (var memoryStack = stackPush()) {
			var memoryProperties = VkPhysicalDeviceMemoryProperties.calloc(memoryStack);
			vkGetPhysicalDeviceMemoryProperties(device.getPhysical(), memoryProperties);

			for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {
				if ((typeFilter & (1 << i)) != 0 &&( memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
					return i;
				}
			}
		}

		throw new RuntimeException("[HuGame] Failed to find suitable memory type");
	}
}
