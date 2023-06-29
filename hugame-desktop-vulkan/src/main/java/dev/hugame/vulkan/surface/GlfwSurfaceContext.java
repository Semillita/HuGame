package dev.hugame.vulkan.surface;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;

import dev.hugame.util.Dimensions;
import dev.hugame.vulkan.core.VulkanInstance;
import dev.hugame.window.DesktopWindow;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.lwjgl.PointerBuffer;

@RequiredArgsConstructor
public class GlfwSurfaceContext implements VulkanSurfaceContext {
  private final DesktopWindow window;

  public VulkanSurface createSurface(VulkanInstance vulkanInstance) {
    return VulkanSurface.createGlfwSurface(vulkanInstance, window.getHandle());
  }

  public Dimensions getSize() {
    try (var memoryStack = stackPush()) {
      var windowWidthBuffer = memoryStack.callocInt(1);
      var windowHeightBuffer = memoryStack.callocInt(1);

      glfwGetFramebufferSize(window.getHandle(), windowWidthBuffer, windowHeightBuffer);

      var width = windowWidthBuffer.get(0);
      var height = windowHeightBuffer.get(0);

      return new Dimensions(width, height);
    }
  }

  public PointerBuffer getRequiredInstanceExtensions() {
    return glfwGetRequiredInstanceExtensions();
  }

  public void addResizeListener(BiConsumer<Integer, Integer> callback) {
    window.addResizeListener(callback);
  }

  public void waitUntilNotMinimized() {
    window.waitUntilNotMinimized();
  }
}
