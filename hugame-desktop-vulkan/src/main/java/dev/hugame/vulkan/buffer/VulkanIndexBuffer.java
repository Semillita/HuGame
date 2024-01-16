package dev.hugame.vulkan.buffer;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import java.nio.ByteBuffer;

public class VulkanIndexBuffer {
  private static final int INDEX_BUFFER_USAGE_FLAGS =
      VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
  private static final int INDEX_BUFFER_PROPERTIES_FLAGS = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;

  public static VulkanIndexBuffer create(VulkanGraphics graphics, int[] indices) {
    var bufferSize = indices.length * Integer.BYTES;

    var buffer =
        BufferUtils.createWithStagingBuffer(
            graphics,
            bufferSize,
            b -> memcpy(b, indices),
            INDEX_BUFFER_USAGE_FLAGS,
            INDEX_BUFFER_PROPERTIES_FLAGS);

    return new VulkanIndexBuffer(buffer, indices.length);
  }

  private static void memcpy(ByteBuffer buffer, int[] indices) {
    for (var index : indices) {
      buffer.putInt(index);
    }
  }

  private final VulkanBuffer buffer;
  private final int indexCount;

  public VulkanIndexBuffer(VulkanBuffer buffer, int indexCount) {
    this.buffer = buffer;
    this.indexCount = indexCount;
  }

  public VulkanBuffer getBuffer() {
    return buffer;
  }

  public long getHandle() {
    return buffer.getHandle();
  }

  public int getIndexCount() {
    return indexCount;
  }
}
