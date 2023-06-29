package io.semillita.hugame.editor.game;

import dev.hugame.util.Dimensions;
import dev.hugame.vulkan.core.VulkanInstance;
import dev.hugame.vulkan.surface.VulkanSurface;
import dev.hugame.vulkan.surface.VulkanSurfaceContext;
import java.awt.AWTException;
import java.awt.Canvas;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.awt.AWTVK;

@RequiredArgsConstructor
public class AwtSurfaceContext implements VulkanSurfaceContext {
  private final Canvas canvas;

  @Override
  public VulkanSurface createSurface(VulkanInstance vulkanInstance) {
    try {
      return VulkanSurface.fromHandle(AWTVK.create(canvas, vulkanInstance.get()));
    } catch (AWTException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Dimensions getSize() {
    return new Dimensions(canvas.getWidth(), canvas.getHeight());
  }

  public PointerBuffer getRequiredInstanceExtensions() {
    var memoryStack = MemoryStack.stackGet();
    return memoryStack.pointers(
        memoryStack.UTF8(KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME),
        memoryStack.UTF8(AWTVK.getSurfaceExtensionName()));
  }

  @Override
  public void addResizeListener(BiConsumer<Integer, Integer> callback) {
    // No resizing yet
  }

  @Override
  public void waitUntilNotMinimized() {
    // No minimizing
  }
}
