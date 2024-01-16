package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK13.*;

import dev.hugame.util.Logger;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;

public class VulkanDebugMessenger {
  public static VulkanDebugMessenger create(VulkanInstance instance) {
    try (var memoryStack = stackPush()) {
      var debugMessengerCreateInfo = makeDebugMessengerCreateInfo(memoryStack);

      var debugMessengerHandleBuffer = memoryStack.longs(VK_NULL_HANDLE);

      if (vkGetInstanceProcAddr(instance.get(), "vkCreateDebugUtilsMessengerEXT")
          == VK_NULL_HANDLE) {
        throw new RuntimeException(
            "[HuGame] Failed to find Vulkan function vkCreateDebugUtilsMessengerEXT");
      }

      if (vkCreateDebugUtilsMessengerEXT(
              instance.get(), debugMessengerCreateInfo, null, debugMessengerHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to set up debug messenger");
      }

      return new VulkanDebugMessenger(debugMessengerHandleBuffer.get(0));
    }
  }

  public static VkDebugUtilsMessengerCreateInfoEXT makeDebugMessengerCreateInfo(
      MemoryStack memoryStack) {
    return VkDebugUtilsMessengerCreateInfoEXT.calloc(memoryStack)
        .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
        .messageSeverity(
            VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        .messageType(
            VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
        .pfnUserCallback(VulkanDebugMessenger::messageCallback);
  }

  private static int messageCallback(
      int messageSeverity, int messageType, long callbackDataHandle, long pUserData) {
    var severity =
        switch (messageSeverity) {
          case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT -> "info";
          case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT -> "verbose";
          case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> "warning";
          case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> "error";
          default -> "unknown severity";
        };

    // Don't close callbackData after usage since the memory is managed by native functions.
    var callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataHandle);

    Logger.log(
        "[HuGame] Vulkan validation (%s): %s".formatted(severity, callbackData.pMessageString()));

    return VK_FALSE;
  }

  private final long debugMessengerHandle;

  private VulkanDebugMessenger(long debugMessengerHandle) {
    this.debugMessengerHandle = debugMessengerHandle;
  }
}
