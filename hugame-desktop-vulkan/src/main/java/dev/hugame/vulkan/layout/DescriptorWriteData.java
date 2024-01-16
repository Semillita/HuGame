package dev.hugame.vulkan.layout;

import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class DescriptorWriteData {
  private final VkWriteDescriptorSet writeData;

  public DescriptorWriteData(VkWriteDescriptorSet writeData) {
    this.writeData = writeData;
  }

  public DescriptorWriteData setDescriptorType(int descriptorType) {
    writeData.descriptorType(descriptorType);
    return this;
  }

  public DescriptorWriteData setDescriptorCount(int descriptorCount) {
    writeData.descriptorCount(descriptorCount);
    return this;
  }

  public DescriptorWriteData setBufferInfo(VkDescriptorBufferInfo.Buffer bufferInfo) {
    writeData.pBufferInfo(bufferInfo);
    return this;
  }

  public DescriptorWriteData setImageInfo(VkDescriptorImageInfo.Buffer imageInfo) {
    writeData.pImageInfo(imageInfo);
    return this;
  }
}
