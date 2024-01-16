package dev.hugame.vulkan.image;

import dev.hugame.vulkan.types.ImageLayout;
import lombok.Getter;
import lombok.Setter;

public class VulkanImage {
  @Getter private final long handle;
  @Getter private final long memoryHandle; // TODO: Can maybe drop this field?
  // TODO: Update when transitioning image layout
  @Getter @Setter private ImageLayout layout;

  public VulkanImage(long handle, long memoryHandle, ImageLayout layout) {
    this.handle = handle;
    this.memoryHandle = memoryHandle;
    this.layout = layout;
  }
}
