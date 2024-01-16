package dev.hugame.vulkan.types;

import static org.lwjgl.vulkan.VK10.*;

import java.util.Arrays;
import lombok.Getter;

public enum ImageViewType {
  _1D(VK_IMAGE_VIEW_TYPE_1D),
  _2D(VK_IMAGE_VIEW_TYPE_2D),
  _3D(VK_IMAGE_VIEW_TYPE_3D),
  _1D_ARRAY(VK_IMAGE_VIEW_TYPE_1D_ARRAY),
  _2D_ARRAY(VK_IMAGE_VIEW_TYPE_2D_ARRAY),
  CUBE(VK_IMAGE_VIEW_TYPE_CUBE),
  CUBE_ARRAY(VK_IMAGE_VIEW_TYPE_CUBE_ARRAY);

  @Getter private final int value;

  ImageViewType(int value) {
    this.value = value;
  }

  public static ImageViewType fromValue(int value) {
    return Arrays.stream(values())
        .filter(imageViewType -> imageViewType.getValue() == value)
        .findAny()
        .orElseThrow(
            () -> new RuntimeException("[HuGame] Unknown Vulkan image view type: " + value));
  }
}
