package dev.hugame.vulkan.sync;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK12.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreTypeCreateInfo;

public class SyncUtils {
  public static BinarySemaphore createBinarySemaphore(VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var semaphoreCreateInfo =
          VkSemaphoreCreateInfo.calloc(memoryStack).sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

      var semaphoreHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateSemaphore(
              graphics.getDevice().getLogical(), semaphoreCreateInfo, null, semaphoreHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create Vulkan semaphore");
      }

      return new BinarySemaphore(semaphoreHandleBuffer.get(0));
    }
  }

  public static TimelineSemaphore createTimelineSemaphore(VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var semaphoreCreateInfo =
          VkSemaphoreCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
              .pNext(
                  VkSemaphoreTypeCreateInfo.calloc(memoryStack)
                      .sType(VK_STRUCTURE_TYPE_SEMAPHORE_TYPE_CREATE_INFO)
                      .semaphoreType(VK_SEMAPHORE_TYPE_TIMELINE)
                      .initialValue(0));

      var semaphoreHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateSemaphore(
              graphics.getDevice().getLogical(), semaphoreCreateInfo, null, semaphoreHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create Vulkan semaphore");
      }

      return new TimelineSemaphore(semaphoreHandleBuffer.get(0));
    }
  }
}
