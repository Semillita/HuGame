package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

public class VulkanCommandPool {
  public static VulkanCommandPool create(VulkanGraphics graphics) {
    var device = graphics.getDevice();
    var queueFamilyIndices = device.getSupport().getQueueFamilyIndices();
    try (var memoryStack = stackPush()) {
      var commandPoolCreateInfo =
          VkCommandPoolCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
              .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
              .queueFamilyIndex(queueFamilyIndices.getGraphicsFamily());

      var commandPoolHandleBuffer = memoryStack.callocLong(1);
      if (vkCreateCommandPool(
              device.getLogical(), commandPoolCreateInfo, null, commandPoolHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create command pool");
      }

      return new VulkanCommandPool(commandPoolHandleBuffer.get(0));
    }
  }

  private final long handle;

  private VulkanCommandPool(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
