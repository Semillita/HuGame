package dev.hugame.vulkan.pipeline;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.*;

public class VulkanRenderPass {
  public static VulkanRenderPass create(VulkanGraphics graphics) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var attachmentDescriptionBuffer = VkAttachmentDescription.calloc(2, memoryStack);

      // Color attachment
      attachmentDescriptionBuffer
          .get(0)
          .format(graphics.getSwapChain().getImageFormat().format())
          .samples(VK_SAMPLE_COUNT_1_BIT)
          .loadOp(VK_ATTACHMENT_LOAD_OP_LOAD)
          .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
          .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
          .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
          .initialLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
          .finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

      var colorAttachmentReferenceBuffer = VkAttachmentReference.calloc(1, memoryStack);
      colorAttachmentReferenceBuffer
          .get(0)
          .attachment(0)
          .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

      // Depth attachment
      attachmentDescriptionBuffer
          .get(1)
          .format(VK_FORMAT_D32_SFLOAT)
          .samples(VK_SAMPLE_COUNT_1_BIT)
          .loadOp(VK_ATTACHMENT_LOAD_OP_LOAD)
          .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
          .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
          .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
          .initialLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
          .finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

      var depthAttachmentReference =
          VkAttachmentReference.calloc(memoryStack)
              .attachment(1)
              .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

      var subpassDescriptionBuffer = VkSubpassDescription.calloc(1, memoryStack);
      subpassDescriptionBuffer
          .get(0)
          .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
          .colorAttachmentCount(1)
          .pColorAttachments(colorAttachmentReferenceBuffer)
          .pDepthStencilAttachment(depthAttachmentReference);

      var subPassDependencyBuffer = VkSubpassDependency.calloc(1, memoryStack);
      subPassDependencyBuffer
          .get(0)
          .srcSubpass(VK_SUBPASS_EXTERNAL)
          .dstSubpass(0)
          .srcStageMask(
              VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
                  | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
          .srcAccessMask(0)
          .dstStageMask(
              VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
                  | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
          .dstAccessMask(
              VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

      var renderPassCreateInfo =
          VkRenderPassCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
              .pAttachments(attachmentDescriptionBuffer)
              .pSubpasses(subpassDescriptionBuffer)
              .pDependencies(subPassDependencyBuffer);

      var renderPassHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateRenderPass(logicalDevice, renderPassCreateInfo, null, renderPassHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create render pass");
      }

      var renderPassHandle = renderPassHandleBuffer.get(0);

      return new VulkanRenderPass(renderPassHandle);
    }
  }

  private final long handle;

  private VulkanRenderPass(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
