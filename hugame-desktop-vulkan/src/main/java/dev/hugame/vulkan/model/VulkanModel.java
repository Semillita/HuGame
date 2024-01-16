package dev.hugame.vulkan.model;

import dev.hugame.graphics.Texture;
import dev.hugame.graphics.model.Model;
import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.texture.TextureArray;
import java.util.List;
import lombok.Getter;

public class VulkanModel implements Model {
  @Getter private final VulkanVertexBuffer vertexBuffer;
  @Getter private final VulkanVertexBuffer instanceBuffer;
  @Getter private final VulkanIndexBuffer indexBuffer;
  @Getter private final List<Texture> textures;
  @Getter private final List<TextureArray> textureArrays;

  public VulkanModel(
      VulkanVertexBuffer vertexBuffer,
      VulkanVertexBuffer instanceBuffer,
      VulkanIndexBuffer indexBuffer,
      List<Texture> textures, /* TODO: Make this be materials instead? */
      List<TextureArray> textureArrays) {
    this.vertexBuffer = vertexBuffer;
    this.instanceBuffer = instanceBuffer;
    this.indexBuffer = indexBuffer;
    this.textures = textures;
    this.textureArrays = textureArrays;
  }

  @Override
  public int getVertexCount() {
    // TODO: Implement or drop?
    return 0;
  }

  @Override
  public int getIndexCount() {
    return indexBuffer.getIndexCount();
  }

  public VulkanIndexBuffer getIndexBuffer() {
    return indexBuffer;
  }
}
