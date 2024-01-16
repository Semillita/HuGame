package dev.hugame.vulkan.sync;

public abstract class Semaphore {
  private final long handle;

  protected Semaphore(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }

  public sealed interface SyncPoint permits BinarySemaphore, TimelineSemaphoreSyncPoint {
    long getSemaphoreHandle();
  }
}
