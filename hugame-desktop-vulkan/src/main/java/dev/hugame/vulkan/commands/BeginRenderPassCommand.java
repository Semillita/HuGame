package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.core.VulkanSwapChainFrameBuffer;
import dev.hugame.vulkan.pipeline.VulkanRenderPass;
import org.lwjgl.vulkan.*;

public class BeginRenderPassCommand extends VulkanCommand {
  private final VulkanRenderPass renderPass;
  private final VulkanSwapChainFrameBuffer frameBuffer;

  public BeginRenderPassCommand(
      VulkanRenderPass renderPass, VulkanSwapChainFrameBuffer frameBuffer) {
    this.renderPass = renderPass;
    this.frameBuffer = frameBuffer;
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      var renderPassBeginInfo =
          VkRenderPassBeginInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
              .renderPass(renderPass.getHandle())
              .framebuffer(frameBuffer.getHandle())
              .renderArea(
                  VkRect2D.calloc(memoryStack)
                      .offset(VkOffset2D.calloc(memoryStack).x(0).y(0))
                      .extent(graphics.getSwapChain().getExtent()));

      vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
    }
  }
}
