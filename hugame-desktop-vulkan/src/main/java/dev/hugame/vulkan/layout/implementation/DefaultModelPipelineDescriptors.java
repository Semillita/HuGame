package dev.hugame.vulkan.layout.implementation;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorFactory;
import dev.hugame.vulkan.layout.VulkanDescriptorPool;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class DefaultModelPipelineDescriptors implements DescriptorFactory {
  private static final int POSITION_SIZE = 3;
  // private static final int COLOR_SIZE = 3;
  private static final int NORMAL_SIZE = 3;
  private static final int TEXTURE_COORDINATES_SIZE = 2;
  // private static final int TEXTURE_INDEX_SIZE = 1;
  private static final int MATERIAL_INDEX_SIZE = 1;
  private static final int TRANSFORM_SIZE = 16;

  private static final int POSITION_OFFSET = 0;
  // private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
  private static final int NORMAL_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
  private static final int TEXTURE_COORDINATES_OFFSET = NORMAL_OFFSET + NORMAL_SIZE * Float.BYTES;
  // private static final int TEXTURE_INDEX_OFFSET =
  // TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE * Float.BYTES;
  private static final int MATERIAL_INDEX_OFFSET =
      TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE * Float.BYTES;

  public static final int VERTEX_SIZE =
      POSITION_SIZE + NORMAL_SIZE + TEXTURE_COORDINATES_SIZE + MATERIAL_INDEX_SIZE;
  private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

  private static final int INSTANCE_SIZE = TRANSFORM_SIZE;
  private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE * Float.BYTES;

  private static final int TRANSFORM_COLUMN_SIZE = 4;
  private static final int TRANSFORM_COLUMN_SIZE_BYTES = TRANSFORM_COLUMN_SIZE * Float.BYTES;

  public VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack memoryStack) {
    var buffer = VkVertexInputBindingDescription.calloc(2, memoryStack);

    // Per-vertex data
    buffer.get(0).binding(0).stride(VERTEX_SIZE_BYTES).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

    // Per-instance data
    buffer.get(1).binding(1).stride(INSTANCE_SIZE_BYTES).inputRate(VK_VERTEX_INPUT_RATE_INSTANCE);

    return buffer;
  }

  public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputAttributeDescription.calloc(8, memoryStack);

    buffer.get(0).binding(0).location(0).format(VK_FORMAT_R32G32B32_SFLOAT).offset(POSITION_OFFSET);

    buffer.get(1).binding(0).location(1).format(VK_FORMAT_R32G32B32_SFLOAT).offset(NORMAL_OFFSET);

    buffer
        .get(2)
        .binding(0)
        .location(2)
        .format(VK_FORMAT_R32G32_SFLOAT)
        .offset(TEXTURE_COORDINATES_OFFSET);

    buffer.get(3).binding(0).location(3).format(VK_FORMAT_R32_SINT).offset(MATERIAL_INDEX_OFFSET);

    // Instance transformation matrix column 1
    buffer.get(4).binding(1).location(4).format(VK_FORMAT_R32G32B32A32_SFLOAT).offset(0);

    // Instance transformation matrix column 2
    buffer
        .get(5)
        .binding(1)
        .location(5)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .offset(TRANSFORM_COLUMN_SIZE_BYTES);

    // Instance transformation matrix column 3
    buffer
        .get(6)
        .binding(1)
        .location(6)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .offset(2 * TRANSFORM_COLUMN_SIZE_BYTES);

    // Instance transformation matrix column 4
    buffer
        .get(7)
        .binding(1)
        .location(7)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .offset(3 * TRANSFORM_COLUMN_SIZE_BYTES);

    return buffer;
  }

  public VulkanDescriptorPool createDescriptorPool(VulkanGraphics graphics) {
    var framesInFlightCount = graphics.getFramesInFlightCount();

    try (var memoryStack = stackPush()) {
      var descriptorPoolSizeBuffer = VkDescriptorPoolSize.calloc(4, memoryStack);

      // Uniform buffer - vertex shader
      descriptorPoolSizeBuffer
          .get(0)
          .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(framesInFlightCount);

      // Uniform buffer - fragment shader
      descriptorPoolSizeBuffer
          .get(1)
          .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(framesInFlightCount);

      // Texture sampler - fragment shader
      descriptorPoolSizeBuffer
          .get(2)
          .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .descriptorCount(framesInFlightCount * 32);

      // Shader storage buffers - fragment shader
      descriptorPoolSizeBuffer
          .get(3)
          .type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
          .descriptorCount(framesInFlightCount * 4);

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
      var descriptorSetLayoutBindingBuffer = VkDescriptorSetLayoutBinding.calloc(7, memoryStack);

      // Uniform buffer - vertex shader
      descriptorSetLayoutBindingBuffer
          .get(0)
          .binding(0)
          .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(
              1) // TODO: Figure out why this is 1 and not the amount of frames in flight
          .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

      // Uniform buffer - fragment shader
      descriptorSetLayoutBindingBuffer
          .get(1)
          .binding(1)
          .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      // Texture sampler
      descriptorSetLayoutBindingBuffer
          .get(2)
          .binding(2)
          .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .descriptorCount(32) // TODO: I should probably allow way more than 32 textures here
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      // Material buffer - fragment shader
      descriptorSetLayoutBindingBuffer
          .get(3)
          .binding(3)
          .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      // Point light buffer - fragment shader
      descriptorSetLayoutBindingBuffer
          .get(4)
          .binding(4)
          .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      // Spot light buffer - fragment shader
      descriptorSetLayoutBindingBuffer
          .get(5)
          .binding(5)
          .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
          .descriptorCount(1)
          .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

      // Directional light buffer - fragment shader
      descriptorSetLayoutBindingBuffer
          .get(6)
          .binding(6)
          .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
          .descriptorCount(1)
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
