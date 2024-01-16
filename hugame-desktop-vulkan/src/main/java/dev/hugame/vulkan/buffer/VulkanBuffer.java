package dev.hugame.vulkan.buffer;

public class VulkanBuffer {
  private final long handle;
  private long memoryHandle;

  public VulkanBuffer(long handle, long memoryHandle) {
    this.handle = handle;
    this.memoryHandle = memoryHandle;
  }

  public long getHandle() {
    return handle;
  }

  public long getMemoryHandle() {
    return memoryHandle;
  }
}
