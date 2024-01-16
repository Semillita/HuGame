package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import dev.hugame.vulkan.pipeline.VulkanPipeline;
import org.lwjgl.vulkan.VkCommandBuffer;

public class BindDescriptorSetsCommand extends VulkanCommand {
  private final VulkanDescriptorSet descriptorSet;
  private final VulkanPipeline pipeline;

  public BindDescriptorSetsCommand(VulkanDescriptorSet descriptorSet, VulkanPipeline pipeline) {
    this.descriptorSet = descriptorSet;
    this.pipeline = pipeline;
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      vkCmdBindDescriptorSets(
          commandBuffer,
          VK_PIPELINE_BIND_POINT_GRAPHICS,
          pipeline.getLayoutHandle(),
          0,
          memoryStack.longs(descriptorSet.getHandle()),
          null);
    }
  }
}
