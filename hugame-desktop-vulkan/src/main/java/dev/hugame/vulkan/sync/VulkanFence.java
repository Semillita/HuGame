package dev.hugame.vulkan.sync;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkFenceCreateInfo;

public class VulkanFence {
  public static VulkanFence create(VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var fenceCreateInfo =
          VkFenceCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
              .flags(VK_FENCE_CREATE_SIGNALED_BIT);

      var fenceHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateFence(graphics.getDevice().getLogical(), fenceCreateInfo, null, fenceHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create Vulkan fence");
      }

      return new VulkanFence(fenceHandleBuffer.get(0));
    }
  }

  private final long handle;

  private VulkanFence(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
