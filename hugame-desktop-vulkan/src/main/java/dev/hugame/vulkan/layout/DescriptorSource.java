package dev.hugame.vulkan.layout;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.buffer.VulkanShaderStorageBuffer;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.texture.TextureArray;
import dev.hugame.vulkan.texture.VulkanTexture;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;

public interface DescriptorSource {
  void write(VulkanGraphics graphics, DescriptorWriteData writeData, MemoryStack memoryStack);

  static DescriptorSource fromUniformBuffer(VulkanUniformBuffer uniformBuffer) {
    return (graphics, writeData, memoryStack) -> {
      var uniformBufferDescriptorBufferInfoBuffer = VkDescriptorBufferInfo.calloc(1, memoryStack);

      uniformBufferDescriptorBufferInfoBuffer
          .get(0)
          .buffer(uniformBuffer.getBuffer().getHandle())
          .offset(0)
          .range(uniformBuffer.getBufferSize());

      writeData
          .setDescriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
          .setDescriptorCount(1)
          .setBufferInfo(uniformBufferDescriptorBufferInfoBuffer);
    };
  }

  // TODO: Can drop? Depends on whether I'll support an API for using non-array textures.
  static DescriptorSource fromTextures(List<VulkanTexture> textures, int samplerSize) {
    return (graphics, writeData, memoryStack) -> {
      var defaultTexture = graphics.getDefaultTexture();

      var descriptorImageInfoBuffer = VkDescriptorImageInfo.calloc(samplerSize, memoryStack);

      for (int i = 0; i < samplerSize; i++) {
        var texture = (textures.size() > i) ? textures.get(i) : defaultTexture;

        descriptorImageInfoBuffer
            .get(i)
            .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
            .imageView(texture.getTextureArray().getImageView().getHandle())
            .sampler(texture.getTextureArray().getImageSampler().getHandle());
      }

      writeData
          .setDescriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .setDescriptorCount(samplerSize)
          .setImageInfo(descriptorImageInfoBuffer);
    };
  }

  static DescriptorSource fromTextureArrays(List<TextureArray> textureArrays, int samplerSize) {
    return (graphics, writeData, memoryStack) -> {
      var defaultTextureArray = graphics.getDefaultTexture().getTextureArray();

      var descriptorImageInfoBuffer = VkDescriptorImageInfo.calloc(samplerSize, memoryStack);

      for (int i = 0; i < samplerSize; i++) {
        var textureArray = (textureArrays.size() > i) ? textureArrays.get(i) : defaultTextureArray;

        descriptorImageInfoBuffer
            .get(i)
            .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
            .imageView(textureArray.getImageView().getHandle())
            .sampler(textureArray.getImageSampler().getHandle());
      }

      writeData
          .setDescriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
          .setDescriptorCount(samplerSize)
          .setImageInfo(descriptorImageInfoBuffer);
    };
  }

  static DescriptorSource fromShaderStorageBuffer(
      VulkanShaderStorageBuffer<?> shaderStorageBuffer) {
    return (graphics, writeData, memoryStack) -> {
      var bufferRange =
          ((long) shaderStorageBuffer.getBytesPerItem()) * shaderStorageBuffer.getMaxItems();

      var shaderStorageBufferDescriptorBufferInfoBuffer =
          VkDescriptorBufferInfo.calloc(1, memoryStack);

      shaderStorageBufferDescriptorBufferInfoBuffer
          .get(0)
          .buffer(shaderStorageBuffer.getBuffer().getHandle())
          .offset(0)
          .range(bufferRange);

      writeData
          .setDescriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
          .setDescriptorCount(1)
          .setBufferInfo(shaderStorageBufferDescriptorBufferInfoBuffer);
    };
  }
}
