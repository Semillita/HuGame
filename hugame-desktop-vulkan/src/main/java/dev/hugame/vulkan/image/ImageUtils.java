package dev.hugame.vulkan.image;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.graphics.ResolvedTexture;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanBuffer;
import dev.hugame.vulkan.commands.CopyBufferToImageCommand;
import dev.hugame.vulkan.commands.PipelineBarrierCommand;
import dev.hugame.vulkan.core.QueueSubmitInfoBuilder;
import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanDevice;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.core.VulkanObject;
import dev.hugame.vulkan.sync.Semaphore;
import dev.hugame.vulkan.types.ImageAspect;
import dev.hugame.vulkan.types.ImageFormat;
import dev.hugame.vulkan.types.ImageLayout;
import dev.hugame.vulkan.types.ImageType;
import java.util.List;
import org.lwjgl.vulkan.*;

public class ImageUtils {
  /*public static VulkanImage createImage(
      VulkanGraphics graphics, ResolvedTexture resolvedTexture, int aspectMask) {
    var dataBuffer = resolvedTexture.buffer();
    var width = resolvedTexture.width();
    var height = resolvedTexture.height();

    var stagingBuffer =
        BufferUtils.createStagingBuffer(graphics, dataBuffer.capacity(), b -> b.put(dataBuffer));

    var image =
        createImage(
            graphics,
            width,
            height,
            1,
            VK_FORMAT_R8G8B8A8_SRGB,
            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);

    transitionImageLayout(
        graphics,
        image,
        VK_FORMAT_R8G8B8A8_SRGB,
        aspectMask,
        VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

    copyBufferToImage(graphics, stagingBuffer, image, width, height);

    transitionImageLayout(
        graphics,
        image,
        VK_FORMAT_R8G8B8A8_SRGB,
        aspectMask,
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

    return image;
  }*/

  public static VulkanImage createImage(
      VulkanGraphics graphics,
      List<ResolvedTexture> resolvedTextures,
      int width,
      int height,
      int aspectMask,
      ImageType imageType) {
    var layerCount = resolvedTextures.size();
    var image =
        createImage(
            graphics,
            width,
            height,
            layerCount,
            VK_FORMAT_R8G8B8A8_SRGB,
            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
            imageType);

    var bufferSize =
        resolvedTextures.stream()
            .mapToInt(resolvedTexture -> resolvedTexture.buffer().capacity())
            .sum();

    var stagingBuffer =
        BufferUtils.createStagingBuffer(
            graphics,
            bufferSize,
            b -> {
              for (var resolvedTexture : resolvedTextures) {
                b.put(resolvedTexture.buffer());
              }
            });

    transitionImageLayout(
        graphics,
        image,
        ImageAspect.fromValue(aspectMask),
        ImageLayout.UNDEFINED,
        ImageLayout.TRANSFER_DESTINATION_OPTIMAL,
        0,
        layerCount);

    copyBufferToImage(graphics, stagingBuffer, image, width, height, 0, layerCount);

    transitionImageLayout(
        graphics,
        image,
        ImageAspect.fromValue(aspectMask),
        ImageLayout.TRANSFER_DESTINATION_OPTIMAL,
        ImageLayout.SHADER_READ_ONLY_OPTIMAL,
        0,
        layerCount);

    return image;
  }

  public static VulkanImage createImage(
      VulkanGraphics graphics,
      int width,
      int height,
      int format,
      int usageFlags,
      ImageType imageType) {
    return createImage(graphics, width, height, 1, format, usageFlags, imageType);
  }

  private static VulkanImage createImage(
      VulkanGraphics graphics,
      int width,
      int height,
      int layerCount,
      int format,
      int usageFlags,
      ImageType imageType) {
    var device = graphics.getDevice();
    var logicalDevice = device.getLogical();

    try (var memoryStack = stackPush()) {

      var imageCreateInfo =
          VkImageCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
              .imageType(imageType.getValue())
              .extent(VkExtent3D.calloc(memoryStack).width(width).height(height).depth(1))
              .mipLevels(1)
              .arrayLayers(layerCount)
              .format(format)
              .tiling(VK_IMAGE_TILING_OPTIMAL)
              .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
              .usage(usageFlags)
              .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
              .samples(VK_SAMPLE_COUNT_1_BIT)
              .flags(0);

      var imageHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateImage(logicalDevice, imageCreateInfo, null, imageHandleBuffer) != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create image");
      }

      var imageHandle = imageHandleBuffer.get(0);

      var imageMemoryRequirements = VkMemoryRequirements.calloc(memoryStack);
      vkGetImageMemoryRequirements(logicalDevice, imageHandle, imageMemoryRequirements);

      var memoryAllocateInfo =
          VkMemoryAllocateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
              .allocationSize(imageMemoryRequirements.size())
              .memoryTypeIndex(
                  findMemoryType(
                      device,
                      imageMemoryRequirements.memoryTypeBits(),
                      VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

      var imageMemoryHandleBuffer = memoryStack.callocLong(1);

      if (vkAllocateMemory(logicalDevice, memoryAllocateInfo, null, imageMemoryHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to allocate memory for texture buffer");
      }

      var imageMemoryHandle = imageMemoryHandleBuffer.get(0);

      vkBindImageMemory(logicalDevice, imageHandle, imageMemoryHandle, 0);

      return new VulkanImage(imageHandle, imageMemoryHandle, ImageLayout.UNDEFINED);
    }
  }

  public static void transitionImageLayout(
      VulkanGraphics graphics,
      VulkanImage image,
      ImageAspect aspect,
      ImageLayout oldLayout,
      ImageLayout newLayout) {
    transitionImageLayout(graphics, image, aspect, oldLayout, newLayout, 0, 1);
  }

  public static void transitionImageLayout(
          VulkanGraphics graphics,
          VulkanImage image,
          ImageAspect aspectMask,
          ImageLayout oldLayout,
          ImageLayout newLayout,
          int baseLayer,
          int layerCount) {
    var commandBuffer = VulkanCommandBuffer.create(graphics);
    var pipelineBarrierCommand =
        new PipelineBarrierCommand(image, oldLayout, newLayout, aspectMask, baseLayer, layerCount);
    commandBuffer.record(graphics, pipelineBarrierCommand);

    var graphicsQueue = graphics.getDevice().getGraphicsQueue();
    graphicsQueue.submitAndWait(commandBuffer);

    commandBuffer.free(graphics);
  }

  public static VulkanCommandBuffer transitionImageLayout(
      VulkanGraphics graphics,
      long imageHandle,
      int aspectMask,
      int oldImageLayout,
      int newImageLayout,
      Semaphore.SyncPoint waitSyncPoint,
      Semaphore.SyncPoint signalSyncPoint) {
    var commandBuffer = VulkanCommandBuffer.create(graphics);
    var pipelineBarrierCommand =
        new PipelineBarrierCommand(imageHandle, oldImageLayout, newImageLayout, aspectMask, 0, 1);
    commandBuffer.record(graphics, pipelineBarrierCommand);

    var graphicsQueue = graphics.getDevice().getGraphicsQueue();
    try (var memoryStack = stackPush()) {
      var submitInfo =
          new QueueSubmitInfoBuilder()
              .setCommandBuffer(commandBuffer)
              .setWaitSyncPoint(waitSyncPoint)
              .setSignalSyncPoint(signalSyncPoint)
              .setWaitDestinationStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
              .build(memoryStack);

      graphicsQueue.submit(submitInfo, null);
    }

    return commandBuffer;
  }

  private static void copyBufferToImage(
      VulkanGraphics graphics,
      VulkanBuffer buffer,
      VulkanImage image,
      int width,
      int height,
      int baseLayer,
      int layerCount) {
    var commandBuffer = VulkanCommandBuffer.create(graphics);

    var copyBufferToImageCommand =
        new CopyBufferToImageCommand(buffer, image, width, height, baseLayer, layerCount);
    commandBuffer.record(graphics, copyBufferToImageCommand);

    graphics.getDevice().getGraphicsQueue().submitAndWait(commandBuffer);
    commandBuffer.free(graphics);
  }

  private static int findMemoryType(VulkanDevice device, int typeFilter, int properties) {
    try (var memoryStack = stackPush()) {
      var memoryProperties = VkPhysicalDeviceMemoryProperties.calloc(memoryStack);
      vkGetPhysicalDeviceMemoryProperties(device.getPhysical(), memoryProperties);

      for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {
        if ((typeFilter & (1 << i)) != 0
            && (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
          return i;
        }
      }
    }

    throw new RuntimeException("[HuGame] Failed to find suitable memory type");
  }
}
