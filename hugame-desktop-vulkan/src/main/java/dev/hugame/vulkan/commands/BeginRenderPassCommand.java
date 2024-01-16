package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanFrameBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.pipeline.VulkanRenderPass;
import org.lwjgl.vulkan.*;

public class BeginRenderPassCommand extends VulkanCommand {
  private final VulkanRenderPass renderPass;
  private final VulkanFrameBuffer frameBuffer;

  public BeginRenderPassCommand(VulkanRenderPass renderPass, VulkanFrameBuffer frameBuffer) {
    this.renderPass = renderPass;
    this.frameBuffer = frameBuffer;
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    var clearColor = graphics.getClearColor();

    try (var memoryStack = stackPush()) {
      // var clearValueBuffer = VkClearValue.calloc(2, memoryStack);

      // TODO: Uncomment and remove!
      /*// Color attachment
      clearValueBuffer.get(0)
      		.color(VkClearColorValue.calloc(memoryStack)
      				.float32(
      						memoryStack.floats(clearColor.x, clearColor.y, clearColor.z, clearColor.w)));

      // Depth attachment
      clearValueBuffer.get(1)
      		.depthStencil(VkClearDepthStencilValue.calloc(memoryStack)
      				.set(1.0f, 0));*/

      var renderPassBeginInfo =
          VkRenderPassBeginInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
              .renderPass(renderPass.getHandle())
              .framebuffer(frameBuffer.getHandle())
              .renderArea(
                  VkRect2D.calloc(memoryStack)
                      .offset(VkOffset2D.calloc(memoryStack).x(0).y(0))
                      .extent(graphics.getSwapChain().getExtent()))
          // .clearValueCount(2)
          /*.pClearValues(clearValueBuffer)*/ ;

      vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
    }
  }
}
