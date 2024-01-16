package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK13.*;

import dev.hugame.util.Logger;
import dev.hugame.vulkan.sync.VulkanFence;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

public class GraphicsQueue {
  private final VkQueue vkQueue;

  public static GraphicsQueue forDevice(VkDevice logicalDevice, Integer queueFamilyIndex) {
    try (var memoryStack = stackPush()) {
      var queuePointerBuffer = memoryStack.callocPointer(1);
      vkGetDeviceQueue(logicalDevice, queueFamilyIndex, 0, queuePointerBuffer);
      var vkQueue = new VkQueue(queuePointerBuffer.get(0), logicalDevice);

      return new GraphicsQueue(vkQueue);
    }
  }

  private GraphicsQueue(VkQueue vkQueue) {
    this.vkQueue = vkQueue;
  }

  public VulkanResult submit(VkSubmitInfo submitInfo, VulkanFence fence) {
    return VulkanResult.fromValue(
        vkQueueSubmit(vkQueue, submitInfo, (fence == null) ? VK_NULL_HANDLE : fence.getHandle()));
  }

  public VulkanResult submit(QueueSubmitInfo submitInfo, VulkanFence fence) {
    try (var memoryStack = stackPush()) {
      var vkSubmitInfo = submitInfo.generateStruct(memoryStack);
      return VulkanResult.fromValue(
          vkQueueSubmit(
              vkQueue, vkSubmitInfo, (fence == null) ? VK_NULL_HANDLE : fence.getHandle()));
    }
  }

  public void submitAndWait(VulkanCommandBuffer commandBuffer) {
    try (var memoryStack = stackPush()) {
      submitAndWait(commandBuffer, memoryStack);
    }
  }

  public void submitAndWait(VulkanCommandBuffer commandBuffer, MemoryStack memoryStack) {
    var submitInfo =
        VkSubmitInfo.calloc(memoryStack)
            .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
            .pCommandBuffers(memoryStack.pointers(commandBuffer.getVkCommandBuffer()));

    submit(submitInfo, null);
    vkQueueWaitIdle(vkQueue);
  }
}
