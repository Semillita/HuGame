package dev.hugame.vulkan.text;

import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.text.Font;
import dev.hugame.graphics.text.TextShaper;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.texture.TextureArray;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

public class TextBatch {
  private static final int MAX_QUAD_COUNT = 1_000;

  private static final int POSITION_SIZE = 3 * Float.BYTES;
  private static final int COLOR_SIZE = 4 * Float.BYTES;
  private static final int TEXTURE_COORDINATES_SIZE = 2 * Float.BYTES;
  private static final int TEXTURE_INDEX_SIZE = Integer.BYTES;
  private static final int TEXTURE_LAYER_SIZE = Integer.BYTES;
  private static final int VERTEX_SIZE =
      POSITION_SIZE
          + COLOR_SIZE
          + TEXTURE_COORDINATES_SIZE
          + TEXTURE_INDEX_SIZE
          + TEXTURE_LAYER_SIZE;
  private static final int BUFFER_SIZE = VERTEX_SIZE * MAX_QUAD_COUNT * 4;

  private static final int POSITION_OFFSET = 0;
  private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE;
  private static final int TEXTURE_COORDINATES_OFFSET = COLOR_OFFSET + COLOR_SIZE;
  private static final int TEXTURE_INDEX_OFFSET =
      TEXTURE_COORDINATES_OFFSET + TEXTURE_COORDINATES_SIZE;
  private static final int TEXTURE_LAYER_OFFSET = TEXTURE_INDEX_OFFSET + TEXTURE_INDEX_SIZE;

  private static final int Z_VALUE = 0;

  @Getter private final VulkanVertexBuffer vertexBuffer;
  @Getter private final VulkanIndexBuffer indexBuffer;
  @Getter private final ByteBuffer vertexDataBuffer;
  @Getter private final Camera2D camera; // TODO: This will only apply to 2D text
  private final int maxTextureSamplerCount;
  private final VulkanGraphics graphics;
  private final TextShaper textShaper;

  private int index;
  @Getter private List<TextureArray> textureArrays = new ArrayList<>();

  public TextBatch(VulkanGraphics graphics, Camera2D camera) {
    this.vertexBuffer = VulkanVertexBuffer.create(graphics, BUFFER_SIZE);
    this.indexBuffer = VulkanIndexBuffer.create(graphics, generateAllQuadIndices());
    this.vertexDataBuffer = MemoryUtil.memCalloc(BUFFER_SIZE);
    vertexDataBuffer.rewind();
    this.camera = camera;
    this.maxTextureSamplerCount =
        graphics.getDevice().getSupport().getLimits().getMaxSampledImages();
    this.graphics = graphics;
    this.textShaper = new TextShaper();
  }

  public void add(String text, Font font, int fontSize, int x, int y) {
    Logger.pushScope("TextBatch#add");

    assert !isFull();

    var atlasTexture = font.getTexture();
    var vulkanTexture = graphics.cast(atlasTexture);
    var textureArray = vulkanTexture.getTextureArray();

    var textureIndex = textureArrays.indexOf(vulkanTexture);
    if (textureIndex == -1) {
      textureArrays.add(textureArray);
      textureIndex = textureArrays.size() - 1;
    }

    var positionedGlyphs = textShaper.shape(text, font);
    var metadataByGlyph = font.getMetadata().glyphs();

    for (var positionedGlyph : positionedGlyphs) {
      var value = positionedGlyph.value();

      // TODO: The glyph offset means the colored part, need to offset the surrounding even more
      var pixelOffsetX = x + positionedGlyph.offsetX() * fontSize;
      var pixelOffsetY = y + (positionedGlyph.offsetY()) * fontSize;

      var pixelWidth = positionedGlyph.width() * fontSize;
      var pixelHeight = positionedGlyph.height() * fontSize;

      var glyphMetadata = metadataByGlyph.get(value);

      /*var u1 = atlasX;
      var v1 = atlasY;
      var u2 = atlasX + glyphMetadata.atlasWidth();
      var v2 = atlasY + glyphMetadata.atlasHeight();*/
      // Logger.log("Rendering glyph with u1=%6f, u2=%6f, v1=%6f, v2=%6f".formatted(u1, u2, v1,
      // v2));

      var pixelToImageWidth = 1d / textureArray.getWidth();
      var pixelToImageHeight = 1d / textureArray.getHeight();

      /*var u1 = atlasX * pixelToImageWidth;
      var v1 = atlasY * pixelToImageHeight;
      var u2 = atlasX + 32 * pixelToImageWidth; // TODO: Use non-hardcoded width and height
      var v2 = atlasY + 32 * pixelToImageHeight;*/

      var u1 = glyphMetadata.atlasX() * pixelToImageWidth;
      var v1 = glyphMetadata.atlasY() * pixelToImageHeight;
      var u2 = (glyphMetadata.atlasX() + 32) * pixelToImageWidth;
      var v2 = (glyphMetadata.atlasY() + 32) * pixelToImageHeight;

      add(
          (float) pixelOffsetX,
          (float) pixelOffsetY,
          (float) pixelWidth,
          (float) pixelHeight,
          (float) u1,
          (float) v1,
          (float) u2,
          (float) v2,
          textureIndex,
          vulkanTexture.getLayer());
    }
    Logger.popScope();
  }

  private void add(
      float x,
      float y,
      float width,
      float height,
      float u1,
      float v1,
      float u2,
      float v2,
      int textureIndex,
      int textureLayer) {
    // TOP LEFT

    // Position
    vertexDataBuffer.putFloat(x);
    vertexDataBuffer.putFloat(y);
    vertexDataBuffer.putFloat(Z_VALUE);

    // Color
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);

    // Texture coordinates
    vertexDataBuffer.putFloat(u1);
    vertexDataBuffer.putFloat(v1);

    // Texture index
    vertexDataBuffer.putInt(textureIndex);

    // Texture layer
    vertexDataBuffer.putInt(textureLayer);

    // BOTTOM LEFT

    // Position
    vertexDataBuffer.putFloat(x);
    vertexDataBuffer.putFloat(y + height);
    vertexDataBuffer.putFloat(Z_VALUE);

    // Color
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);

    // Texture coordinates
    vertexDataBuffer.putFloat(u1);
    vertexDataBuffer.putFloat(v2);
    // Texture index
    vertexDataBuffer.putInt(textureIndex);
    // Texture layer
    vertexDataBuffer.putInt(textureLayer);

    // BOTTOM RIGHT

    // Position
    vertexDataBuffer.putFloat(x + width);
    vertexDataBuffer.putFloat(y + height);
    vertexDataBuffer.putFloat(Z_VALUE);

    // Color
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);

    // Texture coordinates
    vertexDataBuffer.putFloat(u2);
    vertexDataBuffer.putFloat(v2);
    // Texture index
    vertexDataBuffer.putInt(textureIndex);
    // Texture layer
    vertexDataBuffer.putInt(textureLayer);

    // TOP RIGHT

    // Position
    vertexDataBuffer.putFloat(x + width);
    vertexDataBuffer.putFloat(y);
    vertexDataBuffer.putFloat(Z_VALUE);

    // Color
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);
    vertexDataBuffer.putFloat(1);

    // Texture coordinates
    vertexDataBuffer.putFloat(u2);
    vertexDataBuffer.putFloat(v1);

    // Texture index
    vertexDataBuffer.putInt(textureIndex);

    // Texture layer
    vertexDataBuffer.putInt(textureLayer);

    index += VERTEX_SIZE * 4;
  }

  public boolean isFull() {
    return textureArrays.size() >= maxTextureSamplerCount;
  }

  public int getIndexCount() {
    return ((index / VERTEX_SIZE) / 4) * 6;
  }

  public void clear() {
    index = 0;
    textureArrays.clear();
    vertexDataBuffer.rewind();
  }

  // TODO: Optimize by not using stream API
  private static int[] generateAllQuadIndices() {
    return IntStream.range(0, MAX_QUAD_COUNT)
        .flatMap(offset -> Arrays.stream(getQuadIndices(offset * 4)))
        .toArray();
  }

  private static int[] getQuadIndices(int quadOffset) {
    return Stream.of(3, 2, 0, 0, 2, 1).mapToInt(index -> index + quadOffset).toArray();
  }
}
