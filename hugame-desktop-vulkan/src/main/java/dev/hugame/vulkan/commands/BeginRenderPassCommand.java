package dev.hugame.vulkan.commands;

import dev.hugame.vulkan.core.VulkanFrameBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BeginRenderPassCommand extends VulkanCommand {
	private final VulkanFrameBuffer frameBuffer;

	public BeginRenderPassCommand(VulkanFrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
	}

	@Override
	public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
		var pipeline = graphics.getPipeline();
		var renderPass = pipeline.getRenderPass();

		try (var memoryStack = stackPush()) {
			var clearColorBuffer = VkClearValue.calloc(2, memoryStack);

			// Color attachment
			clearColorBuffer.get(0)
					.color(VkClearColorValue.calloc(memoryStack)
							.int32(memoryStack.ints(1, 0, 0, 1)));

			// Depth attachment
			clearColorBuffer.get(1)
					.depthStencil(VkClearDepthStencilValue.calloc(memoryStack)
							.set(1.0f, 0));

			var renderPassBeginInfo = VkRenderPassBeginInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
					.renderPass(renderPass.getHandle())
					.framebuffer(frameBuffer.getHandle())
					.renderArea(
							VkRect2D.calloc(memoryStack)
									.offset(
											VkOffset2D.calloc(memoryStack)
													.x(0)
													.y(0))
									.extent(graphics.getSwapChain().getExtent()))
					.clearValueCount(1)
					.pClearValues(clearColorBuffer);

			vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
		}
	}
}
