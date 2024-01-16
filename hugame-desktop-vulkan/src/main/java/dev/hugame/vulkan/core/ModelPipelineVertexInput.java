package dev.hugame.vulkan.core;

import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

@Deprecated
public class ModelPipelineVertexInput {
  private static final int POSITION_SIZE = 3;
  private static final int COLOR_SIZE = 3;
  private static final int TEXTURE_COORDINATES_SIZE = 2;
  private static final int TEXTURE_INDEX_SIZE = 1;

  private static final int POSITION_OFFSET = 0;
  private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
  private static final int TEXTURE_COORDINATES_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
  private static final int TEXTURE_INDEX_OFFSET =
      TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE * Float.BYTES;

  public static final int VERTEX_SIZE =
      POSITION_SIZE + COLOR_SIZE + TEXTURE_COORDINATES_SIZE + TEXTURE_INDEX_SIZE;
  private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

  private static final int INSTANCE_SIZE = 16;
  private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE * Float.BYTES;

  private static final int TRANSFORM_COLUMN_SIZE = 4;
  private static final int TRANSFORM_COLUMN_SIZE_BYTES = TRANSFORM_COLUMN_SIZE * Float.BYTES;

  public static VkVertexInputBindingDescription.Buffer getBindingDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputBindingDescription.calloc(2, memoryStack);

    // Per-vertex data
    buffer.get(0).binding(0).stride(VERTEX_SIZE_BYTES).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

    // Per-instance data
    buffer.get(1).binding(1).stride(INSTANCE_SIZE_BYTES).inputRate(VK_VERTEX_INPUT_RATE_INSTANCE);

    return buffer;
  }

  public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(
      MemoryStack memoryStack) {
    var buffer = VkVertexInputAttributeDescription.calloc(8, memoryStack);

    buffer.get(0).binding(0).location(0).format(VK_FORMAT_R32G32B32_SFLOAT).offset(POSITION_OFFSET);

    buffer.get(1).binding(0).location(1).format(VK_FORMAT_R32G32B32_SFLOAT).offset(COLOR_OFFSET);

    buffer
        .get(2)
        .binding(0)
        .location(2)
        .format(VK_FORMAT_R32G32_SFLOAT)
        .offset(TEXTURE_COORDINATES_OFFSET);

    buffer.get(3).binding(0).location(3).format(VK_FORMAT_R32_SINT).offset(TEXTURE_INDEX_OFFSET);

    // Instance transformation matrix column 1
    buffer.get(4).binding(1).location(4).format(VK_FORMAT_R32G32B32A32_SFLOAT).offset(0);

    // Instance transformation matrix column 2
    buffer
        .get(5)
        .binding(1)
        .location(5)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .offset(TRANSFORM_COLUMN_SIZE_BYTES);

    // Instance transformation matrix column 3
    buffer
        .get(6)
        .binding(1)
        .location(6)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .offset(2 * TRANSFORM_COLUMN_SIZE_BYTES);

    // Instance transformation matrix column 4
    buffer
        .get(7)
        .binding(1)
        .location(7)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .offset(3 * TRANSFORM_COLUMN_SIZE_BYTES);

    return buffer;
  }
}
