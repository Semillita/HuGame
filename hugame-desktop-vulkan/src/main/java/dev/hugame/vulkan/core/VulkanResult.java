package dev.hugame.vulkan.core;

import dev.hugame.util.Logger;

import static org.lwjgl.vulkan.VK10.*;

import java.util.Arrays;

public enum VulkanResult {
  SUCCESS(VK_SUCCESS),
  DEVICE_LOST(VK_ERROR_DEVICE_LOST),
  OUT_OF_DEVICE_MEMORY(VK_ERROR_OUT_OF_DEVICE_MEMORY),
  OUT_OF_HOST_MEMORY(VK_ERROR_OUT_OF_HOST_MEMORY),
  UNKNOWN(-1);

  private final int value;

  VulkanResult(int value) {
    this.value = value;
  }

  public void assertSuccess() {
    if (this != SUCCESS) {
      throw new RuntimeException("[Error] Expected success result");
    }
  }

  public static VulkanResult fromValue(int value) {
    return Arrays.stream(values())
        .filter(result -> result.value == value)
        .findAny()
        .orElseGet(
            () -> {
              Logger.error("Unknown Vulkan result code: " + value);
              return UNKNOWN;
            });
  }
}
