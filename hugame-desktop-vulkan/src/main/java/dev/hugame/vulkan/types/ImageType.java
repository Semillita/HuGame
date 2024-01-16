package dev.hugame.vulkan.types;

import static org.lwjgl.vulkan.VK10.*;

import java.util.Arrays;
import lombok.Getter;

public enum ImageType {
  _1D(VK_IMAGE_TYPE_1D),
  _2D(VK_IMAGE_TYPE_2D),
  _3D(VK_IMAGE_TYPE_3D);

  @Getter private final int value;

  ImageType(int value) {
    this.value = value;
  }

  public static ImageType fromValue(int value) {
    return Arrays.stream(values())
        .filter(imageType -> imageType.getValue() == value)
        .findAny()
        .orElseThrow(() -> new RuntimeException("[HuGame] Unknown Vulkan image type: " + value));
  }
}
