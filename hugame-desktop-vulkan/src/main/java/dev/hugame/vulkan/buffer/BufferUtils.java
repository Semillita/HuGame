package dev.hugame.vulkan.buffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;

import dev.hugame.util.Logger;
import dev.hugame.vulkan.commands.CopyBufferCommand;
import dev.hugame.vulkan.core.VulkanCommandBuffer;
import dev.hugame.vulkan.core.VulkanDevice;
import dev.hugame.vulkan.core.VulkanGraphics;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class BufferUtils {
  private static final int STAGING_BUFFER_READ_USAGE_FLAGS = VK_BUFFER_USAGE_TRANSFER_DST_BIT;
  private static final int STAGING_BUFFER_WRITE_USAGE_FLAGS = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
  private static final int STAGING_BUFFER_PROPERTIES_FLAGS =
      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

  public static VulkanPersistentlyMappedBuffer createPersistentlyMappedBuffer(
      VulkanGraphics graphics, int bufferSize, int usageFlags, int propertiesFlags) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var bufferHandleBuffer = memoryStack.callocLong(1);
      var bufferMemoryHandleBuffer = memoryStack.callocLong(1);

      createBuffer(
          graphics,
          bufferSize,
          usageFlags,
          propertiesFlags,
          bufferHandleBuffer,
          bufferMemoryHandleBuffer);

      var bufferHandle = bufferHandleBuffer.get(0);
      var bufferMemoryHandle = bufferMemoryHandleBuffer.get(0);

      var mappedDataHandleBuffer = memoryStack.callocPointer(1);

      vkMapMemory(logicalDevice, bufferMemoryHandle, 0, bufferSize, 0, mappedDataHandleBuffer);

      var mappedMemory = mappedDataHandleBuffer.getByteBuffer(bufferSize);

      return new VulkanPersistentlyMappedBuffer(bufferHandle, bufferMemoryHandle, mappedMemory);
    }
  }

  public static VulkanBuffer createStagingBuffer(
      VulkanGraphics graphics, int bufferSize, Consumer<ByteBuffer> fillBuffer) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var bufferHandleBuffer = memoryStack.callocLong(1);
      var bufferMemoryHandleBuffer = memoryStack.callocLong(1);

      createBuffer(
          graphics,
          bufferSize,
          STAGING_BUFFER_WRITE_USAGE_FLAGS,
          STAGING_BUFFER_PROPERTIES_FLAGS,
          bufferHandleBuffer,
          bufferMemoryHandleBuffer);

      var bufferHandle = bufferHandleBuffer.get(0);
      var bufferMemoryHandle = bufferMemoryHandleBuffer.get(0);

      var dataPointer = memoryStack.callocPointer(1);
      vkMapMemory(logicalDevice, bufferMemoryHandle, 0, bufferSize, 0, dataPointer);
      var dataBuffer = dataPointer.getByteBuffer(bufferSize);
      fillBuffer.accept(dataBuffer);
      vkUnmapMemory(logicalDevice, bufferMemoryHandle);

      return new VulkanBuffer(bufferHandle, bufferMemoryHandle);
    }
  }

  public static VulkanBuffer createWithStagingBuffer(
      VulkanGraphics graphics,
      int bufferSize,
      Consumer<ByteBuffer> fillBuffer,
      int usageFlags,
      int propertiesFlags) {
    var device = graphics.getDevice();
    var logicalDevice = device.getLogical();

    try (var memoryStack = stackPush()) {
      var stagingBufferHandleBuffer = memoryStack.callocLong(1);
      var stagingBufferMemoryHandleBuffer = memoryStack.callocLong(1);

      createBuffer(
          graphics,
          bufferSize,
          STAGING_BUFFER_WRITE_USAGE_FLAGS,
          STAGING_BUFFER_PROPERTIES_FLAGS,
          stagingBufferHandleBuffer,
          stagingBufferMemoryHandleBuffer);

      var stagingBufferHandle = stagingBufferHandleBuffer.get(0);
      var stagingBufferMemoryHandle = stagingBufferMemoryHandleBuffer.get(0);

      var dataPointer = memoryStack.callocPointer(1);
      vkMapMemory(logicalDevice, stagingBufferMemoryHandle, 0, bufferSize, 0, dataPointer);
      fillBuffer.accept(dataPointer.getByteBuffer(bufferSize));
      vkUnmapMemory(logicalDevice, stagingBufferMemoryHandle);

      var actualBufferHandleBuffer = memoryStack.callocLong(1);
      var actualBufferMemoryHandleBuffer = memoryStack.callocLong(1);

      createBuffer(
          graphics,
          bufferSize,
          usageFlags,
          propertiesFlags,
          actualBufferHandleBuffer,
          actualBufferMemoryHandleBuffer);

      var actualBufferHandle = actualBufferHandleBuffer.get(0);
      var actualBufferMemoryHandle = actualBufferMemoryHandleBuffer.get(0);

      copyBuffer(graphics, stagingBufferHandle, actualBufferHandle, bufferSize);

      vkDestroyBuffer(logicalDevice, stagingBufferHandle, null);
      vkFreeMemory(logicalDevice, stagingBufferMemoryHandle, null);

      return new VulkanBuffer(actualBufferHandle, actualBufferMemoryHandle);
    }
  }

  public static VulkanBuffer createDynamicBuffer(
      VulkanGraphics graphics, int bufferSize, int usageFlags, int propertiesFlags) {
    try (var memoryStack = stackPush()) {
      var handleBuffer = memoryStack.callocLong(1);
      var memoryHandleBuffer = memoryStack.callocLong(1);

      createBuffer(
          graphics, bufferSize, usageFlags, propertiesFlags, handleBuffer, memoryHandleBuffer);

      var handle = handleBuffer.get(0);
      var memoryHandle = memoryHandleBuffer.get(0);

      return new VulkanBuffer(handle, memoryHandle);
    }
  }

  public static void fillWithStagingBuffer(
      VulkanGraphics graphics, VulkanBuffer buffer, ByteBuffer data) {
    var device = graphics.getDevice();
    var logicalDevice = device.getLogical();
    var bufferSize = data.capacity();

    var stagingBuffer =
        createStagingBuffer(graphics, bufferSize, stagingBufferData -> stagingBufferData.put(data));

    var stagingBufferHandle = stagingBuffer.getHandle();
    var stagingBufferMemoryHandle = stagingBuffer.getMemoryHandle();

    copyBuffer(graphics, stagingBufferHandle, buffer.getHandle(), bufferSize);

    vkDestroyBuffer(logicalDevice, stagingBufferHandle, null);
    vkFreeMemory(logicalDevice, stagingBufferMemoryHandle, null);
  }

  public static void readDeviceBuffer(
      VulkanGraphics graphics, VulkanBuffer deviceBuffer, ByteBuffer dataBuffer, int bufferSize) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var stagingBufferHandleBuffer = memoryStack.callocLong(1);
      var stagingBufferMemoryHandleBuffer = memoryStack.callocLong(1);

      createBuffer(
          graphics,
          bufferSize,
          STAGING_BUFFER_READ_USAGE_FLAGS,
          STAGING_BUFFER_PROPERTIES_FLAGS,
          stagingBufferHandleBuffer,
          stagingBufferMemoryHandleBuffer);

      var stagingBufferHandle = stagingBufferHandleBuffer.get(0);
      var stagingBufferMemoryHandle = stagingBufferMemoryHandleBuffer.get(0);

      copyBuffer(graphics, deviceBuffer.getHandle(), stagingBufferHandle, bufferSize);

      var hostBufferPointerBuffer = memoryStack.callocPointer(1);
      vkMapMemory(
          logicalDevice, stagingBufferMemoryHandle, 0, bufferSize, 0, hostBufferPointerBuffer);
      var hostBuffer = hostBufferPointerBuffer.getByteBuffer(bufferSize);
      dataBuffer.put(hostBuffer);
      vkUnmapMemory(logicalDevice, stagingBufferMemoryHandle);
    }
  }

  public static List<String> inspectDeviceBufferBytes(
      VulkanGraphics graphics, VulkanBuffer deviceBuffer, int bufferSize) {
    try (var memoryStack = stackPush()) {
      var dataBuffer = memoryStack.calloc(bufferSize);
      BufferUtils.readDeviceBuffer(graphics, deviceBuffer, dataBuffer, bufferSize);

      var bytes = new ArrayList<String>();
      for (int i = 0; i < bufferSize; i++) {
        var b = dataBuffer.get(i);
        var hexadecimal = String.format("0x%02X", b);
        bytes.add(hexadecimal);
      }

      return bytes;
    }
  }

  public static void destroyBuffer(VulkanGraphics graphics, VulkanBuffer buffer) {
    var logicalDevice = graphics.getDevice().getLogical();

    vkDestroyBuffer(logicalDevice, buffer.getHandle(), null);
    vkFreeMemory(logicalDevice, buffer.getMemoryHandle(), null);
  }

  private static void createBuffer(
      VulkanGraphics graphics,
      int bufferSize,
      int usageFlags,
      int propertyFlags,
      LongBuffer bufferHandleBuffer,
      LongBuffer bufferMemoryHandleBuffer) {
    var device = graphics.getDevice();
    var logicalDevice = device.getLogical();

    try (var memoryStack = stackPush()) {
      var bufferCreateInto =
          VkBufferCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
              .size(bufferSize)
              .usage(usageFlags)
              .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

      if (vkCreateBuffer(logicalDevice, bufferCreateInto, null, bufferHandleBuffer) != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create buffer");
      }

      var bufferHandle = bufferHandleBuffer.get(0);

      var memoryRequirements = VkMemoryRequirements.calloc(memoryStack);
      vkGetBufferMemoryRequirements(logicalDevice, bufferHandle, memoryRequirements);

      var memoryAllocateInfo =
          VkMemoryAllocateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
              .allocationSize(memoryRequirements.size())
              .memoryTypeIndex(
                  findMemoryType(device, memoryRequirements.memoryTypeBits(), propertyFlags));

      if (vkAllocateMemory(logicalDevice, memoryAllocateInfo, null, bufferMemoryHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to allocate buffer memory");
      }

      var bufferMemoryHandle = bufferMemoryHandleBuffer.get(0);

      vkBindBufferMemory(logicalDevice, bufferHandle, bufferMemoryHandle, 0);
    }
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

  private static void copyBuffer(
      VulkanGraphics graphics, long sourceBufferHandle, long destinationBufferHandle, long size) {
    try (var memoryStack = stackPush()) {
      var temporaryCommandBuffer = VulkanCommandBuffer.create(graphics);
      var copyBufferCommand =
          new CopyBufferCommand(sourceBufferHandle, destinationBufferHandle, size);

      temporaryCommandBuffer.record(graphics, copyBufferCommand);
      graphics.getDevice().getGraphicsQueue().submitAndWait(temporaryCommandBuffer, memoryStack);
      temporaryCommandBuffer.free(graphics);
    }
  }
}
