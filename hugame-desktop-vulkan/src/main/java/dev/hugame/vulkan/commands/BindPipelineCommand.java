package dev.hugame.vulkan.commands;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.pipeline.VulkanPipeline;
import org.lwjgl.vulkan.VkCommandBuffer;

public class BindPipelineCommand extends VulkanCommand {
  private final long pipelineHandle;

  public BindPipelineCommand(VulkanPipeline pipeline) {
    this.pipelineHandle = pipeline.getHandle();
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineHandle);
  }
}
