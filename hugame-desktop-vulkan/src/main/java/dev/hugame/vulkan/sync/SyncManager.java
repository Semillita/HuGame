package dev.hugame.vulkan.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyncManager {
  private final Map<TimelineSemaphore, TimelineSemaphoreSyncPoint> latestSyncPointBySemaphore;

  public SyncManager() {
    latestSyncPointBySemaphore = new HashMap<>();
  }

  public Optional<TimelineSemaphoreSyncPoint> getLatestSyncPoint(TimelineSemaphore semaphore) {
    return Optional.ofNullable(latestSyncPointBySemaphore.get(semaphore));
  }

  public TimelineSemaphoreSyncPoint addSyncPoint(TimelineSemaphore semaphore) {
    var latestSyncPoint = latestSyncPointBySemaphore.get(semaphore);
    var newSyncPointValue = (latestSyncPoint == null) ? 1 : (latestSyncPoint.getValue() + 1);

    var newSyncPoint = new TimelineSemaphoreSyncPoint(semaphore, newSyncPointValue);
    latestSyncPointBySemaphore.put(semaphore, newSyncPoint);

    return newSyncPoint;
  }

  public void clear() {
    latestSyncPointBySemaphore.clear();
  }
}
