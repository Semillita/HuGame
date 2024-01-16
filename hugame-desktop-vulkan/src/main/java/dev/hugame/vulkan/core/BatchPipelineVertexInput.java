package dev.hugame.vulkan.core;

import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

@Deprecated
public class BatchPipelineVertexInput {
  private static final int POSITION_SIZE_BYTES = 3 * Float.BYTES;
  private static final int TEXTURE_COORDINATES_SIZE_BYTES = 2 * Float.BYTES;
  private static final int TEXTURE_INDEX_SIZE = Float.BYTES;

  private static final int POSITION_OFFSET = 0;
  private static final int TEXTURE_COORDINATES_OFFSET = POSITION_OFFSET + POSITION_SIZE_BYTES;
  private static final int TEXTURE_INDEX_OFFSET =
      TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE_BYTES * Float.BYTES;

  private static final int VERTEX_SIZE_BYTES =
      POSITION_SIZE_BYTES + TEXTURE_COORDINATES_SIZE_BYTES + TEXTURE_INDEX_SIZE;

  public static VkVertexInputBindingDescription.Buffer getBindingDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputBindingDescription.calloc(1, memoryStack);

    // Per-vertex data
    buffer.get(0).binding(0).stride(VERTEX_SIZE_BYTES).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

    // TODO: Look into using instancing quads and having per-instance texture index.

    return buffer;
  }

  public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputAttributeDescription.calloc(8, memoryStack);

    buffer.get(0).binding(0).location(0).format(VK_FORMAT_R32G32B32_SFLOAT).offset(POSITION_OFFSET);

    buffer
        .get(2)
        .binding(0)
        .location(1)
        .format(VK_FORMAT_R32G32_SFLOAT)
        .offset(TEXTURE_COORDINATES_OFFSET);

    buffer.get(3).binding(0).location(2).format(VK_FORMAT_R32_SINT).offset(TEXTURE_INDEX_OFFSET);

    return buffer;
  }
}
