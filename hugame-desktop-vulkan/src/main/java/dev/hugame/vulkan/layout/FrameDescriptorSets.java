package dev.hugame.vulkan.layout;

import java.util.List;

public class FrameDescriptorSets {
  private final List<VulkanDescriptorSet> descriptorSets;

  public FrameDescriptorSets(List<VulkanDescriptorSet> descriptorSets) {
    this.descriptorSets = descriptorSets;
  }

  public List<VulkanDescriptorSet> getDescriptorSets() {
    return descriptorSets;
  }
}
