package dev.hugame.vulkan.types;

import static org.lwjgl.vulkan.VK10.*;

import java.util.Arrays;
import lombok.Getter;

public enum ImageAspect {
  COLOR(VK_IMAGE_ASPECT_COLOR_BIT),
  DEPTH(VK_IMAGE_ASPECT_DEPTH_BIT),
  METADATA(VK_IMAGE_ASPECT_METADATA_BIT),
  STENCIL(VK_IMAGE_ASPECT_STENCIL_BIT);

  @Getter private final int value;

  ImageAspect(int value) {
    this.value = value;
  }

  public static ImageAspect fromValue(int value) {
    return Arrays.stream(values())
        .filter(imageAspect -> imageAspect.getValue() == value)
        .findAny()
        .orElseThrow(() -> new RuntimeException("[HuGame] Unknown Vulkan image aspect: " + value));
  }
}
