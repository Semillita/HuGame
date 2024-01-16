package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

// TODO: Later create this from some CommandExecutorFactory with its own command pool for
// short-lived buffers
public interface CommandExecutor {
  void execute(VulkanCommand... commands);
}
