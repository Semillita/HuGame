package dev.hugame.vulkan.layout.implementation;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SINT;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorFactory;
import dev.hugame.vulkan.layout.VulkanDescriptorPool;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class DefaultQuadPipelineDescriptors implements DescriptorFactory {
  private static final int POSITION_SIZE_BYTES = 3 * Float.BYTES;
  private static final int TEXTURE_COORDINATES_SIZE_BYTES = 2 * Float.BYTES;
  private static final int TEXTURE_INDEX_SIZE_BYTES = Integer.BYTES;
  private static final int TEXTURE_LAYER_SIZE_BYTES = Integer.BYTES;

  private static final int POSITION_OFFSET = 0;
  private static final int TEXTURE_COORDINATES_OFFSET = POSITION_OFFSET + POSITION_SIZE_BYTES;
  private static final int TEXTURE_INDEX_OFFSET =
      TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE_BYTES;
  private static final int TEXTURE_LAYER_OFFSET = TEXTURE_INDEX_OFFSET + TEXTURE_INDEX_SIZE_BYTES;

  private static final int VERTEX_SIZE_BYTES =
      POSITION_SIZE_BYTES
          + TEXTURE_COORDINATES_SIZE_BYTES
          + TEXTURE_INDEX_SIZE_BYTES
          + TEXTURE_LAYER_SIZE_BYTES;

  public VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack memoryStack) {
    var buffer = VkVertexInputBindingDescription.calloc(1, memoryStack);

    buffer.get(0).binding(0).stride(VERTEX_SIZE_BYTES).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

    // TODO: Look into using instancing quads and having per-instance texture index.

    return buffer;
  }

  public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputAttributeDescription.calloc(4, memoryStack);

    buffer.get(0).binding(0).location(0).format(VK_FORMAT_R32G32B32_SFLOAT).offset(POSITION_OFFSET);

    buffer
        .get(1)
        .binding(0)
        .location(1)
        .format(VK_FORMAT_R32G32_SFLOAT)
        .offset(TEXTURE_COORDINATES_OFFSET);

    buffer.get(2).binding(0).location(2).format(VK_FORMAT_R32_SINT).offset(TEXTURE_INDEX_OFFSET);

    buffer.get(3).binding(0).location(3).format(VK_FORMAT_R32_SINT).offset(TEXTURE_LAYER_OFFSET);

    return buffer;
  }

  public VulkanDescriptorPool createDescriptorPool(VulkanGraphics graphics) {
    var framesInFlightCount = graphics.getFramesInFlightCount();

    try (var memoryStack = stackPush()) {
      var descriptorPoolSizeBuffer = VkDescriptorPoolSize.calloc(2, memoryStack);

      // UBO
      descriptorPoolSizeBuffer
          .get(0)
          .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(framesInFlightCount);

      // Texture sampler
      descriptorPoolSizeBuffer
          .get(1)
          .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .descriptorCount(framesInFlightCount * 32); // TODO: Make count a constant.

      var descriptorPoolCreateInfo =
          VkDescriptorPoolCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
              .pPoolSizes(descriptorPoolSizeBuffer)
              .maxSets(framesInFlightCount);

      var descriptorPoolHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateDescriptorPool(
              graphics.getDevice().getLogical(),
              descriptorPoolCreateInfo,
              null,
              descriptorPoolHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create descriptor pool");
      }

      var descriptorPoolHandle = descriptorPoolHandleBuffer.get(0);

      return new VulkanDescriptorPool(descriptorPoolHandle);
    }
  }

  public VulkanDescriptorSetLayout createDescriptorSetLayout(VulkanGraphics graphics) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var descriptorSetLayoutBindingBuffer = VkDescriptorSetLayoutBinding.calloc(2, memoryStack);

      // UBO
      descriptorSetLayoutBindingBuffer
          .get(0)
          .binding(0)
          .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

      // Texture sampler
      descriptorSetLayoutBindingBuffer
          .get(1)
          .binding(1)
          .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .descriptorCount(32)
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      var descriptorSetLayoutCreateInfo =
          VkDescriptorSetLayoutCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
              .pBindings(descriptorSetLayoutBindingBuffer);

      var descriptorSetLayoutHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateDescriptorSetLayout(
              logicalDevice, descriptorSetLayoutCreateInfo, null, descriptorSetLayoutHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create descriptor set layout");
      }

      var descriptorSetLayoutHandle = descriptorSetLayoutHandleBuffer.get(0);

      return new VulkanDescriptorSetLayout(descriptorSetLayoutHandle);
    }
  }
}
