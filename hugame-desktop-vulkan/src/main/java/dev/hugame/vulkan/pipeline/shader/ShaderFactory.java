package dev.hugame.vulkan.pipeline.shader;

import dev.hugame.core.Graphics;
import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class ShaderFactory {
	public static long createShaderModule(VulkanGraphics graphics, ByteBuffer data) {
		try (var memoryStack = stackPush()) {
			System.out.println("Creating shader module with data size " + data.capacity());
			var shaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
					.pCode(data);

			var shaderModuleHandleBuffer = memoryStack.callocLong(1);

			if (vkCreateShaderModule(
					graphics.getDevice().getLogical(),
					shaderModuleCreateInfo,
					null,
					shaderModuleHandleBuffer) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to create shader module");
			}

			return shaderModuleHandleBuffer.get(0);
		}
	}
}
