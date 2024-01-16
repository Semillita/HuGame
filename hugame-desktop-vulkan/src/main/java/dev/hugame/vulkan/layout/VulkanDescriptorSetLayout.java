package dev.hugame.vulkan.layout;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorSetLayout {
	public static VulkanDescriptorSetLayout create(VulkanGraphics graphics) {
		var logicalDevice = graphics.getDevice().getLogical();

		try (var memoryStack = stackPush()) {
			var descriptorSetLayoutBindingBuffer = VkDescriptorSetLayoutBinding.calloc(2, memoryStack);

			// UBO
			descriptorSetLayoutBindingBuffer.get(0)
					.binding(0)
					.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
					.descriptorCount(1)
					.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

			// Texture sampler
			descriptorSetLayoutBindingBuffer.get(1)
					.binding(1)
					.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.descriptorCount(1)
					.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

			var descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
					.pBindings(descriptorSetLayoutBindingBuffer);

			var descriptorSetLayoutHandleBuffer = memoryStack.callocLong(1);

			if (vkCreateDescriptorSetLayout(logicalDevice, descriptorSetLayoutCreateInfo, null, descriptorSetLayoutHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to create descriptor set layout");
			}

			var descriptorSetLayoutHandle = descriptorSetLayoutHandleBuffer.get(0);

			return new VulkanDescriptorSetLayout(descriptorSetLayoutHandle);
		}
	}

	private final long handle;

	public VulkanDescriptorSetLayout(long handle) {
		this.handle = handle;
	}

	public long getHandle() {
		return handle;
	}
}
