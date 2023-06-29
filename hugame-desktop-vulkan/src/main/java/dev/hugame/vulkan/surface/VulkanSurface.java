package dev.hugame.vulkan.surface;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK13.*;

import dev.hugame.vulkan.core.VulkanInstance;

// TODO: Make this an interface implemented by GlfwVulkanSurface and EditorVulkanSurface
public class VulkanSurface {
  public static VulkanSurface createGlfwSurface(VulkanInstance instance, long windowHandle) {
    try (var memoryStack = stackPush()) {
      var surfaceHandleBuffer = memoryStack.callocLong(1);

      if (glfwCreateWindowSurface(instance.get(), windowHandle, null, surfaceHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create window surface");
      }

      var surfaceHandle = surfaceHandleBuffer.get(0);

      return new VulkanSurface(surfaceHandle);
    }
  }

  public static VulkanSurface fromHandle(long handle) {
    return new VulkanSurface(handle);
  }

  private final long handle;

  private VulkanSurface(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
