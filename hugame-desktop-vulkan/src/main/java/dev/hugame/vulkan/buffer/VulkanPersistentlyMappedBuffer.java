package dev.hugame.vulkan.buffer;

import java.nio.ByteBuffer;

public class VulkanPersistentlyMappedBuffer extends VulkanBuffer {
  private final ByteBuffer mappedMemory;

  public VulkanPersistentlyMappedBuffer(long handle, long memoryHandle, ByteBuffer mappedMemory) {
    super(handle, memoryHandle);

    this.mappedMemory = mappedMemory;
  }

  public ByteBuffer getMappedMemory() {
    return mappedMemory;
  }
}
