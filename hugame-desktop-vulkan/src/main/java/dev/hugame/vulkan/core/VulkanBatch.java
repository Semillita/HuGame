package dev.hugame.vulkan.core;

import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Texture;
import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.texture.TextureArray;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

public class VulkanBatch implements Batch {
  private static final int MAX_QUAD_COUNT = 1000;

  private static final int POSITION_SIZE_BYTES = 3 * Float.BYTES;
  private static final int TEXTURE_COORDINATES_SIZE_BYTES = 2 * Float.BYTES;
  private static final int TEXTURE_INDEX_SIZE_BYTES = Integer.BYTES;
  private static final int TEXTURE_LAYER_SIZE_BYTES = Integer.BYTES;

  private static final int VERTEX_SIZE_BYTES =
      POSITION_SIZE_BYTES
          + TEXTURE_COORDINATES_SIZE_BYTES
          + TEXTURE_INDEX_SIZE_BYTES
          + TEXTURE_LAYER_SIZE_BYTES;

  private static final int BUFFER_SIZE_BYTES = MAX_QUAD_COUNT * 4 * VERTEX_SIZE_BYTES;

  private final VulkanGraphics graphics;
  private final VulkanVertexBuffer vertexBuffer;
  private final VulkanIndexBuffer indexBuffer;
  private final int maxTextureSamplerCount;
  private final ByteBuffer vertexDataBuffer;

  private int index;
  @Getter private List<TextureArray> textureArrays;
  private Camera2D camera;

  public VulkanBatch(VulkanGraphics graphics) {
    this.graphics = graphics;

    this.vertexBuffer = VulkanVertexBuffer.create(graphics, BUFFER_SIZE_BYTES);
    this.indexBuffer = VulkanIndexBuffer.create(graphics, generateAllQuadIndices());

    this.maxTextureSamplerCount =
        graphics.getDevice().getSupport().getLimits().getMaxSampledImages();

    this.vertexDataBuffer = MemoryUtil.memCalloc(BUFFER_SIZE_BYTES);

    this.textureArrays = new ArrayList<>();
  }

  @Override
  public void begin() {
    index = 0;
  }

  @Override
  public void end() {
    flush();
  }

  @Override
  public void draw(Texture texture, int x, int y, int width, int height) {
    if (index / (4 * VERTEX_SIZE_BYTES) >= MAX_QUAD_COUNT
        || textureArrays.size() >= maxTextureSamplerCount) {
      flush();
    }

    var vulkanTexture = graphics.cast(texture);
    var textureArray = vulkanTexture.getTextureArray();

    var textureSlot = textureArrays.indexOf(vulkanTexture);
    if (textureSlot == -1) {
      textureArrays.add(textureArray);
      textureSlot = textureArrays.size() - 1;
    }

    final float u1 = 0f, v1 = 1f, u2 = 1f, v2 = 0f;

    var z = 0;

    // TOP LEFT

    // Position
    vertexDataBuffer.putFloat(x);
    vertexDataBuffer.putFloat(y);
    vertexDataBuffer.putFloat(z);
    // Texture coordinates
    vertexDataBuffer.putFloat(u1);
    vertexDataBuffer.putFloat(v1);
    // Texture index
    vertexDataBuffer.putInt(textureSlot);
    // Texture layer
    vertexDataBuffer.putInt(vulkanTexture.getLayer());

    index += VERTEX_SIZE_BYTES;

    // BOTTOM LEFT

    // Position
    vertexDataBuffer.putFloat(x);
    vertexDataBuffer.putFloat(y + height);
    vertexDataBuffer.putFloat(z);
    // Texture coordinates
    vertexDataBuffer.putFloat(u1);
    vertexDataBuffer.putFloat(v2);
    // Texture index
    vertexDataBuffer.putInt(textureSlot);
    // Texture layer
    vertexDataBuffer.putInt(vulkanTexture.getLayer());

    index += VERTEX_SIZE_BYTES;

    // BOTTOM RIGHT

    // Position
    vertexDataBuffer.putFloat(x + width);
    vertexDataBuffer.putFloat(y + height);
    vertexDataBuffer.putFloat(z);
    // Texture coordinates
    vertexDataBuffer.putFloat(u2);
    vertexDataBuffer.putFloat(v2);
    // Texture index
    vertexDataBuffer.putInt(textureSlot);
    // Texture layer
    vertexDataBuffer.putInt(vulkanTexture.getLayer());

    index += VERTEX_SIZE_BYTES;

    // TOP RIGHT

    // Position
    vertexDataBuffer.putFloat(x + width);
    vertexDataBuffer.putFloat(y);
    vertexDataBuffer.putFloat(z);
    // Texture coordinates
    vertexDataBuffer.putFloat(u2);
    vertexDataBuffer.putFloat(v1);
    // Texture index
    vertexDataBuffer.putInt(textureSlot);
    // Texture layer
    vertexDataBuffer.putInt(vulkanTexture.getLayer());

    index += VERTEX_SIZE_BYTES;
  }

  @Override
  public void setCamera(Camera2D camera) {
    this.camera = camera;
  }

  @Override
  public void flush() {
    vertexDataBuffer.flip();
    graphics.getRenderer().renderBatch(this);

    textureArrays.clear();
    index = 0;
    vertexDataBuffer.rewind();
  }

  public VulkanVertexBuffer getVertexBuffer() {
    return vertexBuffer;
  }

  public VulkanIndexBuffer getIndexBuffer() {
    return indexBuffer;
  }

  public int getIndexCount() {
    return ((index / VERTEX_SIZE_BYTES) / 4) * 6;
  }

  public Camera2D getCamera() {
    return camera;
  }

  public ByteBuffer getVertexDataBuffer() {
    return vertexDataBuffer;
  }

  private static int[] generateAllQuadIndices() {
    return IntStream.range(0, MAX_QUAD_COUNT)
        .flatMap(offset -> Arrays.stream(getQuadIndices(offset * 4)))
        .toArray();
  }

  private static int[] getQuadIndices(int quadOffset) {
    return Stream.of(3, 2, 0, 0, 2, 1).mapToInt(index -> index + quadOffset).toArray();
  }
}
