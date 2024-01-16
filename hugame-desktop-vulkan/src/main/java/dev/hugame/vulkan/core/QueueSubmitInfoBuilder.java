package dev.hugame.vulkan.core;

import static org.lwjgl.vulkan.VK12.*;

import dev.hugame.vulkan.sync.Semaphore;
import dev.hugame.vulkan.sync.TimelineSemaphoreSyncPoint;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkTimelineSemaphoreSubmitInfo;

public class QueueSubmitInfoBuilder {
  private VulkanCommandBuffer commandBuffer;
  private Semaphore.SyncPoint waitSyncPoint;
  private Semaphore.SyncPoint signalSyncPoint;
  private Integer waitDestinationStageMask;

  public QueueSubmitInfoBuilder setCommandBuffer(VulkanCommandBuffer commandBuffer) {
    this.commandBuffer = commandBuffer;

    return this;
  }

  public QueueSubmitInfoBuilder setWaitSyncPoint(Semaphore.SyncPoint waitSyncPoint) {
    this.waitSyncPoint = waitSyncPoint;
    return this;
  }

  public QueueSubmitInfoBuilder setSignalSyncPoint(Semaphore.SyncPoint signalSyncPoint) {
    this.signalSyncPoint = signalSyncPoint;
    return this;
  }

  public QueueSubmitInfoBuilder setWaitDestinationStageMask(int waitDestinationStageMask) {
    this.waitDestinationStageMask = waitDestinationStageMask;

    return this;
  }

  public VkSubmitInfo build(MemoryStack memoryStack) {
    var submitInfo = VkSubmitInfo.calloc(memoryStack).sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

    if (commandBuffer != null) {
      submitInfo.pCommandBuffers(memoryStack.pointers(commandBuffer.getVkCommandBuffer()));
    }

    if (waitSyncPoint instanceof TimelineSemaphoreSyncPoint
        || signalSyncPoint instanceof TimelineSemaphoreSyncPoint) {
      var timelineSemaphoreSubmitInfo =
          VkTimelineSemaphoreSubmitInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_TIMELINE_SEMAPHORE_SUBMIT_INFO);

      if (waitSyncPoint instanceof TimelineSemaphoreSyncPoint waitTimelineSemaphoreSyncPoint) {
        timelineSemaphoreSubmitInfo.waitSemaphoreValueCount(1);
        timelineSemaphoreSubmitInfo.pWaitSemaphoreValues(
            memoryStack.longs(waitTimelineSemaphoreSyncPoint.getValue()));
      }

      if (signalSyncPoint instanceof TimelineSemaphoreSyncPoint signalTimelineSemaphoreSyncPoint) {
        timelineSemaphoreSubmitInfo.signalSemaphoreValueCount(1);
        timelineSemaphoreSubmitInfo.pSignalSemaphoreValues(
            memoryStack.longs(signalTimelineSemaphoreSyncPoint.getValue()));
      }

      submitInfo.pNext(timelineSemaphoreSubmitInfo);
    }

    if (waitSyncPoint != null) {
      submitInfo
          .waitSemaphoreCount(1)
          .pWaitSemaphores(memoryStack.longs(waitSyncPoint.getSemaphoreHandle()));
    }

    if (signalSyncPoint != null) {
      submitInfo.pSignalSemaphores(memoryStack.longs(signalSyncPoint.getSemaphoreHandle()));
    }

    if (waitDestinationStageMask != null) {
      submitInfo.pWaitDstStageMask(memoryStack.ints(waitDestinationStageMask));
    }

    return submitInfo;
  }
}
