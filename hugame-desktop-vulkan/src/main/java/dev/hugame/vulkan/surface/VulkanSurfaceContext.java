package dev.hugame.vulkan.surface;

import dev.hugame.util.Dimensions;
import dev.hugame.vulkan.core.VulkanInstance;
import java.util.function.BiConsumer;
import org.lwjgl.PointerBuffer;

public interface VulkanSurfaceContext {
  VulkanSurface createSurface(VulkanInstance vulkanInstance);

  Dimensions getSize();

  PointerBuffer getRequiredInstanceExtensions();

  void addResizeListener(BiConsumer<Integer, Integer> callback);

  void waitUntilNotMinimized();
}
