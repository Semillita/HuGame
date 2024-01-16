package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK13.*;

import java.util.Set;
import java.util.stream.Collectors;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkLayerProperties;

public class VulkanValidations {
  private static final Set<String> VALIDATION_LAYERS = Set.of("VK_LAYER_KHRONOS_validation");

  public static void assertValidationLayersSupported() {
    try (var memoryStack = stackPush()) {
      var layerCountBuffer = memoryStack.ints(0);
      vkEnumerateInstanceLayerProperties(layerCountBuffer, null);

      var availableLayers = VkLayerProperties.malloc(layerCountBuffer.get(0), memoryStack);
      vkEnumerateInstanceLayerProperties(layerCountBuffer, availableLayers);

      var availableLayerNames =
          availableLayers.stream()
              .map(VkLayerProperties::layerNameString)
              .collect(Collectors.toSet());

      if (!availableLayerNames.containsAll(VALIDATION_LAYERS)) {
        throw new RuntimeException("[HuGame] Failed to initialize validation layers");
      }
    }
  }

  public static PointerBuffer getValidationLayersAsPointerBuffer(MemoryStack memoryStack) {
    return VulkanUtils.asPointerBuffer(memoryStack, VALIDATION_LAYERS);
  }
}
