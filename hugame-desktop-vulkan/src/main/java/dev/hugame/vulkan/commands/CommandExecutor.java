package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanDevice;
import dev.hugame.vulkan.core.VulkanGraphics;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

// TODO: Later create this from some CommandExecutorFactory with its own command pool for short-lived buffers
public interface CommandExecutor {
	void execute(VulkanCommand... commands);
}
