package dev.hugame.vulkan.types;

import static org.lwjgl.vulkan.VK10.*;

import java.util.Arrays;
import lombok.Getter;

public enum ImageFormat {
  R8_G8_B8_A8_SRGB(VK_FORMAT_R8G8B8A8_SRGB);

  @Getter private final int value;

  ImageFormat(int value) {
    this.value = value;
  }

  public static ImageFormat fromValue(int value) {
    return Arrays.stream(values())
        .filter(imageFormat -> imageFormat.getValue() == value)
        .findAny()
        .orElseThrow(() -> new RuntimeException("[HuGame] Unknown Vulkan image format: " + value));
  }
}
