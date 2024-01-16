package dev.hugame.vulkan.buffer;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.util.Logger;
import dev.hugame.vulkan.core.*;
import java.nio.ByteBuffer;

public class VulkanVertexBuffer {
  private static final int VERTEX_BUFFER_USAGE_FLAGS =
      VK_BUFFER_USAGE_TRANSFER_DST_BIT
          | VK_BUFFER_USAGE_TRANSFER_SRC_BIT
          | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
  private static final int VERTEX_BUFFER_PROPERTIES_FLAGS = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;

  public static VulkanVertexBuffer create(VulkanGraphics graphics, float[] vertices) {
    var bufferSize = vertices.length * Float.BYTES;

    var buffer =
        BufferUtils.createWithStagingBuffer(
            graphics,
            bufferSize,
            b -> memcpy(b, vertices),
            VERTEX_BUFFER_USAGE_FLAGS,
            VERTEX_BUFFER_PROPERTIES_FLAGS);

    return new VulkanVertexBuffer(buffer);
  }

  public static VulkanVertexBuffer create(VulkanGraphics graphics, int bufferSize) {
    var buffer =
        BufferUtils.createDynamicBuffer(
            graphics, bufferSize, VERTEX_BUFFER_USAGE_FLAGS, VERTEX_BUFFER_PROPERTIES_FLAGS);

    return new VulkanVertexBuffer(buffer);
  }

  private static void memcpy(ByteBuffer buffer, float[] vertices) {
    for (var vertexValue : vertices) {
      buffer.putFloat(vertexValue);
    }
  }

  private final VulkanBuffer buffer;

  private VulkanVertexBuffer(VulkanBuffer buffer) {
    this.buffer = buffer;
  }

  public VulkanBuffer getBuffer() {
    return buffer;
  }

  public long getHandle() {
    return buffer.getHandle();
  }
}
