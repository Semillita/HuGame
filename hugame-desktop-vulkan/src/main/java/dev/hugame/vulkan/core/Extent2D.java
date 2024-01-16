package dev.hugame.vulkan.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;

public class Extent2D {
  private final int width;
  private final int height;

  public Extent2D(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public VkExtent2D toNativeStruct(MemoryStack memoryStack) {
    return VkExtent2D.calloc(memoryStack).set(width, height);
  }
}
