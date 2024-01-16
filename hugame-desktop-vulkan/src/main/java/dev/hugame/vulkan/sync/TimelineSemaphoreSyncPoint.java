package dev.hugame.vulkan.sync;

import dev.hugame.vulkan.core.VulkanObject;

public final class TimelineSemaphoreSyncPoint implements Semaphore.SyncPoint {
  private final TimelineSemaphore semaphore;
  private final int value;

  public TimelineSemaphoreSyncPoint(TimelineSemaphore semaphore, int value) {
    this.semaphore = semaphore;
    this.value = value;
  }

  @Override
  public long getSemaphoreHandle() {
    return semaphore.getHandle();
  }

  @Override
  public String toString() {
    return "TimelineSemaphore[%s]-SyncPoint[%d]"
        .formatted(VulkanObject.formatHandle(getSemaphoreHandle()), value);
  }

  public int getValue() {
    return value;
  }
}
