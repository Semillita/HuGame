package dev.hugame.vulkan.core;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanSurface {
  public static VulkanSurface create(VulkanGraphics graphics, long windowHandle) {
    try (var memoryStack = stackPush()) {
      var surfaceHandleBuffer = memoryStack.callocLong(1);

      if (glfwCreateWindowSurface(
              graphics.getInstance().get(), windowHandle, null, surfaceHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create window surface");
      }

      var surfaceHandle = surfaceHandleBuffer.get(0);

      return new VulkanSurface(surfaceHandle);
    }
  }

  private final long handle;

  private VulkanSurface(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
