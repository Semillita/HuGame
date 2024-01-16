package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK13.*;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;

public class PresentQueue {
  private final VkQueue vkQueue;

  public static PresentQueue forDevice(VkDevice logicalDevice, Integer queueFamilyIndex) {
    try (var memoryStack = stackPush()) {
      var queuePointerBuffer = memoryStack.callocPointer(1);
      vkGetDeviceQueue(logicalDevice, queueFamilyIndex, 0, queuePointerBuffer);
      var vkQueue = new VkQueue(queuePointerBuffer.get(0), logicalDevice);

      return new PresentQueue(vkQueue);
    }
  }

  private PresentQueue(VkQueue vkQueue) {
    this.vkQueue = vkQueue;
  }

  public int present(VkPresentInfoKHR presentInfo) {
    return vkQueuePresentKHR(vkQueue, presentInfo);
  }
}
