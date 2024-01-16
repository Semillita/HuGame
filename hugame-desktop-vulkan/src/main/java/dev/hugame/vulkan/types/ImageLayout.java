package dev.hugame.vulkan.types;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.util.Arrays;
import lombok.Getter;

public enum ImageLayout {
  COLOR_ATTACHMENT_OPTIMAL(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL),
  DEPTH_STENCIL_ATTACHMENT_OPTIMAL(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL),
  DEPTH_STENCIL_READ_ONLY_OPTIMAL(VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL),
  GENERAL(VK_IMAGE_LAYOUT_GENERAL),
  PREINITIALIZED(VK_IMAGE_LAYOUT_PREINITIALIZED),
  PRESENT_SOURCE(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR),
  SHADER_READ_ONLY_OPTIMAL(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL),
  TRANSFER_DESTINATION_OPTIMAL(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL),
  TRANSFER_SOURCE_OPTIMAL(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL),
  UNDEFINED(VK_IMAGE_LAYOUT_UNDEFINED);

  @Getter private final int value;

  ImageLayout(int value) {
    this.value = value;
  }

  public static ImageLayout fromValue(int value) {
    return Arrays.stream(values())
        .filter(layout -> layout.getValue() == value)
        .findAny()
        .orElseThrow(() -> new RuntimeException("[HuGame] Unknown Vulkan image layout: " + value));
  }
}
