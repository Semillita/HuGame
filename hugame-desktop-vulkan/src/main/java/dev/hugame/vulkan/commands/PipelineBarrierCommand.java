package dev.hugame.vulkan.commands;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.image.VulkanImage;
import dev.hugame.vulkan.types.ImageAspect;
import dev.hugame.vulkan.types.ImageLayout;
import lombok.RequiredArgsConstructor;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;

@RequiredArgsConstructor
public class PipelineBarrierCommand extends VulkanCommand {
  private final long imageHandle; // TODO: Make this a VulkanImage
  private final int oldImageLayout;
  private final int newImageLayout;
  private final int aspectMask;
  private final int baseLayer;
  private final int layerCount;

  public PipelineBarrierCommand(
      VulkanImage image,
      ImageLayout oldImageLayout,
      ImageLayout newImageLayout,
      ImageAspect aspectMask,
      int baseLayer,
      int layerCount) {
    this(
        image.getHandle(),
        oldImageLayout.getValue(),
        newImageLayout.getValue(),
        aspectMask.getValue(),
        baseLayer,
        layerCount);
  }

  @Override
  public void record(VkCommandBuffer commandBuffer, VulkanGraphics graphics) {
    try (var memoryStack = stackPush()) {
      int sourceStage, destinationStage, sourceAccessMask, destinationAccessMask;

      if (oldImageLayout == VK_IMAGE_LAYOUT_UNDEFINED
          && newImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
        sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        sourceAccessMask = 0;
        destinationAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
      } else if (oldImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
          && newImageLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
        sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
        sourceAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
        destinationAccessMask = VK_ACCESS_SHADER_READ_BIT;
      } else if (oldImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
          && newImageLayout == VK_IMAGE_LAYOUT_PRESENT_SRC_KHR) {
        sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        destinationStage = VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
        sourceAccessMask = 0;
        destinationAccessMask = 0;
      } else if ((oldImageLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
              || oldImageLayout == VK_IMAGE_LAYOUT_UNDEFINED)
          && newImageLayout == VK_IMAGE_LAYOUT_PRESENT_SRC_KHR) {
        sourceStage = VK_PIPELINE_STAGE_HOST_BIT;
        destinationStage = VK_PIPELINE_STAGE_HOST_BIT;
        sourceAccessMask = 0;
        destinationAccessMask = 0;
      } else if (oldImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
          && newImageLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
        // No idea whether this configuration is right
        sourceStage = VK_PIPELINE_STAGE_HOST_BIT;
        destinationStage = VK_PIPELINE_STAGE_HOST_BIT;
        sourceAccessMask = 0;
        destinationAccessMask = 0;
      } else if (oldImageLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
          && newImageLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
        // No idea whether this configuration is right
        sourceStage = VK_PIPELINE_STAGE_HOST_BIT;
        destinationStage = VK_PIPELINE_STAGE_HOST_BIT;
        sourceAccessMask = 0;
        destinationAccessMask = 0;
      } else if (oldImageLayout == VK_IMAGE_LAYOUT_UNDEFINED
          && newImageLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
        // No idea whether this configuration is right
        sourceStage = VK_PIPELINE_STAGE_HOST_BIT;
        destinationStage = VK_PIPELINE_STAGE_HOST_BIT;
        sourceAccessMask = 0;
        destinationAccessMask = 0;
      } else {
        throw new RuntimeException("[HuGame] Unsupported layout transition");
      }

      var imageMemoryBarrierBuffer = VkImageMemoryBarrier.calloc(1, memoryStack);
      imageMemoryBarrierBuffer
          .get(0)
          .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
          .oldLayout(oldImageLayout)
          .newLayout(newImageLayout)
          .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
          .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
          .image(imageHandle)
          .subresourceRange(
              VkImageSubresourceRange.calloc(memoryStack)
                  .aspectMask(aspectMask)
                  .baseMipLevel(0)
                  .levelCount(1)
                  .baseArrayLayer(baseLayer)
                  .layerCount(layerCount))
          .srcAccessMask(sourceAccessMask)
          .dstAccessMask(destinationAccessMask);

      vkCmdPipelineBarrier(
          commandBuffer, sourceStage, destinationStage, 0, null, null, imageMemoryBarrierBuffer);
    }
  }
}
