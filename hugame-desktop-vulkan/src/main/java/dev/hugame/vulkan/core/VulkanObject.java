package dev.hugame.vulkan.core;

public class VulkanObject {
  public static String formatHandle(long handle) {
    var characterCount = Long.BYTES * 2;
    var hexString = Long.toHexString(handle);

    /*if (hexString.length() < characterCount) {
      hexString = "0".repeat(characterCount - hexString.length()) + hexString;
    }*/

    return hexString;
  }
}
