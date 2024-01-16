package dev.hugame.vulkan.layout;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.texture.VulkanTexture;
import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorPool {
	public static VulkanDescriptorPool create(VulkanGraphics graphics) {
		var framesInFlightCount = graphics.getFramesInFlightCount();

		try (var memoryStack = stackPush()) {
			var descriptorPoolSizeBuffer = VkDescriptorPoolSize.calloc(2, memoryStack);

			// UBO
			descriptorPoolSizeBuffer.get(0).type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
					.descriptorCount(framesInFlightCount);

			// Texture sampler
			descriptorPoolSizeBuffer.get(1).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.descriptorCount(framesInFlightCount);

			var descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
					.pPoolSizes(descriptorPoolSizeBuffer)
					.maxSets(framesInFlightCount);

			var descriptorPoolHandleBuffer = memoryStack.callocLong(1);

			if (vkCreateDescriptorPool(
					graphics.getDevice().getLogical(),
					descriptorPoolCreateInfo,
					null,
					descriptorPoolHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to create descriptor pool");
			}

			var descriptorPoolHandle = descriptorPoolHandleBuffer.get(0);

			return new VulkanDescriptorPool(descriptorPoolHandle);
		}
	}

	private final long handle;

	private VulkanDescriptorPool(long handle) {
		this.handle = handle;
	}

	public long getHandle() {
		return handle;
	}

	public List<VulkanDescriptorSet> allocateDescriptorSets(VulkanGraphics graphics, VulkanTexture texture) {
		var descriptorSetLayout = graphics.getDescriptorSetLayout();
		var framesInFlightCount = graphics.getFramesInFlightCount();
		var uniformBuffers = graphics.getUniformBuffers();
		var logicalDevice = graphics.getDevice().getLogical();
		var descriptorSetsPerFrame = 2;

		try (var memoryStack = stackPush()) {
			var descriptorSetLayoutHandles = memoryStack.longs(
					IntStream.range(0, framesInFlightCount)
							.mapToLong(ignored -> descriptorSetLayout.getHandle())
							.toArray());

			var descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
					.descriptorPool(graphics.getDescriptorPool().getHandle())
					.pSetLayouts(descriptorSetLayoutHandles);

			var descriptorSetHandleBuffer = memoryStack.callocLong(framesInFlightCount);

			if (vkAllocateDescriptorSets(logicalDevice, descriptorSetAllocateInfo, descriptorSetHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to allocate descriptor sets");
			}

			var descriptorSets = new ArrayList<VulkanDescriptorSet>();

			for (int i  = 0; i < framesInFlightCount; i++) {
				var descriptorBufferInfoBuffer = VkDescriptorBufferInfo.calloc(1, memoryStack);
				descriptorBufferInfoBuffer.get(0)
						.buffer(uniformBuffers.get(i).getBuffer().getHandle())
						.offset(0)
						.range(3 * 16 * Float.BYTES);

				var descriptorImageInfoBuffer = VkDescriptorImageInfo.calloc(1, memoryStack);
				descriptorImageInfoBuffer.get(0)
						.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
						.imageView(texture.getImageView().getHandle())
						.sampler(texture.getImageSampler().getHandle());

				var descriptorSetHandle = descriptorSetHandleBuffer.get(i);

				var descriptorWriteBuffer = VkWriteDescriptorSet.calloc(2, memoryStack);

				// UBO
				descriptorWriteBuffer.get(0)
						.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
						.dstSet(descriptorSetHandle)
						.dstBinding(0)
						.dstArrayElement(0)
						.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.pBufferInfo(descriptorBufferInfoBuffer)
						.pImageInfo(null);

				// Texture sampler
				descriptorWriteBuffer.get(1)
						.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
						.dstSet(descriptorSetHandle)
						.dstBinding(1)
						.dstArrayElement(0)
						.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
						.pImageInfo(descriptorImageInfoBuffer);

				vkUpdateDescriptorSets(logicalDevice, descriptorWriteBuffer, null);

				var descriptorSet = new VulkanDescriptorSet(descriptorSetHandle);
				descriptorSets.add(descriptorSet);
			}

			return descriptorSets;
		}
	}

}
