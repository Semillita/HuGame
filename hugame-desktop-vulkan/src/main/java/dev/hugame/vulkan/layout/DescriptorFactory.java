package dev.hugame.vulkan.layout;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public interface DescriptorFactory {
  VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack memoryStack);

  VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack memoryStack);

  VulkanDescriptorPool createDescriptorPool(VulkanGraphics graphics);

  VulkanDescriptorSetLayout createDescriptorSetLayout(VulkanGraphics graphics);
}
