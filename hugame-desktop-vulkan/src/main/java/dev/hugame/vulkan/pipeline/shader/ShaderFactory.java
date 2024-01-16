package dev.hugame.vulkan.pipeline.shader;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import java.nio.ByteBuffer;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public class ShaderFactory {
  public static long createShaderModule(VulkanGraphics graphics, ByteBuffer data) {
    try (var memoryStack = stackPush()) {
      var shaderModuleCreateInfo =
          VkShaderModuleCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
              .pCode(data);

      var shaderModuleHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateShaderModule(
              graphics.getDevice().getLogical(),
              shaderModuleCreateInfo,
              null,
              shaderModuleHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create shader module");
      }

      return shaderModuleHandleBuffer.get(0);
    }
  }
}
