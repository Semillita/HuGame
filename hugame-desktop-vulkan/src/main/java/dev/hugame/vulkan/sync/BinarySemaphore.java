package dev.hugame.vulkan.sync;

import dev.hugame.vulkan.core.VulkanObject;

public final class BinarySemaphore extends Semaphore implements Semaphore.SyncPoint {
  public BinarySemaphore(long handle) {
    super(handle);
  }

  @Override
  public long getSemaphoreHandle() {
    return getHandle();
  }

  @Override
  public String toString() {
    return "BinarySemaphore[%s]".formatted(VulkanObject.formatHandle(getHandle()));
  }
}
