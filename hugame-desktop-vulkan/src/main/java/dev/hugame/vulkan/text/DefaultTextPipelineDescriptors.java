package dev.hugame.vulkan.text;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorFactory;
import dev.hugame.vulkan.layout.VulkanDescriptorPool;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class DefaultTextPipelineDescriptors implements DescriptorFactory {
  private static final int POSITION_SIZE = 3 * Float.BYTES;
  private static final int COLOR_SIZE = 4 * Float.BYTES;
  private static final int TEXTURE_COORDINATES_SIZE = 2 * Float.BYTES;
  private static final int TEXTURE_INDEX_SIZE = Integer.BYTES;
  private static final int TEXTURE_LAYER_SIZE = Integer.BYTES;
  private static final int VERTEX_SIZE =
      POSITION_SIZE
          + COLOR_SIZE
          + TEXTURE_COORDINATES_SIZE
          + TEXTURE_INDEX_SIZE
          + TEXTURE_LAYER_SIZE;

  private static final int POSITION_OFFSET = 0;
  private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE;
  private static final int TEXTURE_COORDINATES_OFFSET = COLOR_OFFSET + COLOR_SIZE;
  private static final int TEXTURE_INDEX_OFFSET =
      TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE;
  private static final int TEXTURE_LAYER_OFFSET = TEXTURE_INDEX_OFFSET + TEXTURE_INDEX_SIZE;

  @Override
  public VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack memoryStack) {
    var buffer = VkVertexInputBindingDescription.calloc(1, memoryStack);

    buffer.get(0).binding(0).stride(VERTEX_SIZE).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

    return buffer;
  }

  @Override
  public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputAttributeDescription.calloc(5, memoryStack);

    buffer.get(0).binding(0).location(0).format(VK_FORMAT_R32G32B32_SFLOAT).offset(POSITION_OFFSET);

    buffer.get(1).binding(0).location(1).format(VK_FORMAT_R32G32B32A32_SFLOAT).offset(COLOR_OFFSET);

    buffer
        .get(2)
        .binding(0)
        .location(2)
        .format(VK_FORMAT_R32G32_SFLOAT)
        .offset(TEXTURE_COORDINATES_OFFSET);

    buffer.get(3).binding(0).location(3).format(VK_FORMAT_R32_SINT).offset(TEXTURE_INDEX_OFFSET);

    buffer.get(4).binding(0).location(4).format(VK_FORMAT_R32_SINT).offset(TEXTURE_LAYER_OFFSET);

    return buffer;
  }

  @Override
  public VulkanDescriptorPool createDescriptorPool(VulkanGraphics graphics) {
    var framesInFlightCount = graphics.getFramesInFlightCount();

    try (var memoryStack = stackPush()) {
      var descriptorPoolSizeBuffer = VkDescriptorPoolSize.calloc(3, memoryStack);

      // Vertex shader uniform buffer
      descriptorPoolSizeBuffer
          .get(0)
          .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(framesInFlightCount);

      // Fragment shader uniform buffer
      descriptorPoolSizeBuffer
          .get(1)
          .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(framesInFlightCount);

      // Texture sampler
      descriptorPoolSizeBuffer
          .get(2)
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

  @Override
  public VulkanDescriptorSetLayout createDescriptorSetLayout(VulkanGraphics graphics) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var descriptorSetLayoutBindingBuffer = VkDescriptorSetLayoutBinding.calloc(3, memoryStack);

      // Vertex shader uniform buffer
      descriptorSetLayoutBindingBuffer
          .get(0)
          .binding(0)
          .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

      // Fragment shader uniform buffer
      descriptorSetLayoutBindingBuffer
          .get(1)
          .binding(1)
          .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      // Fragment shader texture sampler
      descriptorSetLayoutBindingBuffer
          .get(2)
          .binding(2)
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
