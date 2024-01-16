package dev.hugame.vulkan.layout;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorSetLayout {
  private final long handle;

  public VulkanDescriptorSetLayout(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
