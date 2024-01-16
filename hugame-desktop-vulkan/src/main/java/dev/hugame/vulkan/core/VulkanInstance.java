package dev.hugame.vulkan.core;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK13.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class VulkanInstance {
  public static VulkanInstance create(boolean validationLayersEnabled) {
    try (var memoryStack = stackPush()) {
      var applicationInfo =
          VkApplicationInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
              .pApplicationName(memoryStack.UTF8Safe("HuGame Application"))
              .applicationVersion(VK_MAKE_VERSION(0, 0, 1))
              .pEngineName(memoryStack.UTF8Safe("HuGame Vulkan Graphics Engine"))
              .engineVersion(VK_MAKE_VERSION(0, 0, 1))
              .apiVersion(VK_API_VERSION_1_2);

      var instanceCreateInfo =
          VkInstanceCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
              .pApplicationInfo(applicationInfo)
              .ppEnabledExtensionNames(
                  getRequiredExtensionsBuffer(memoryStack, validationLayersEnabled));

      if (validationLayersEnabled) {
        instanceCreateInfo
            .ppEnabledLayerNames(VulkanValidations.getValidationLayersAsPointerBuffer(memoryStack))
            .pNext(VulkanDebugMessenger.makeDebugMessengerCreateInfo(memoryStack).address());
      }

      var instancePointerBuffer = memoryStack.mallocPointer(1);

      if (vkCreateInstance(instanceCreateInfo, null, instancePointerBuffer) != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create Vulkan instance");
      }

      var vkInstance = new VkInstance(instancePointerBuffer.get(0), instanceCreateInfo);

      return new VulkanInstance(vkInstance);
    }
  }

  private static PointerBuffer getRequiredExtensionsBuffer(
      MemoryStack memoryStack, boolean enableValidationLayers) {
    var glfwExtensionsBuffer = glfwGetRequiredInstanceExtensions();

    if (enableValidationLayers) {
      return memoryStack
          .mallocPointer(glfwExtensionsBuffer.capacity() + 1)
          .put(glfwExtensionsBuffer)
          .put(memoryStack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
          .rewind();
    }

    return glfwExtensionsBuffer;
  }

  private final VkInstance vkInstance;

  private VulkanInstance(VkInstance vkInstance) {
    this.vkInstance = vkInstance;
  }

  public VkInstance get() {
    return vkInstance;
  }

  public void destroy() {
    vkDestroyInstance(vkInstance, null);
  }
}
