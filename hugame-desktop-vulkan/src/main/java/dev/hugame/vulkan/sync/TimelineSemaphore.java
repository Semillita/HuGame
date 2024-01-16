package dev.hugame.vulkan.sync;

import dev.hugame.vulkan.core.VulkanObject;

public class TimelineSemaphore extends Semaphore {
  public TimelineSemaphore(long handle) {
    super(handle);
  }

  @Override
  public String toString() {
    return "TimelineSemaphore[%s]".formatted(VulkanObject.formatHandle(getHandle()));
  }
}
