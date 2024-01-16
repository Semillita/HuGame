package dev.hugame.vulkan.texture;

import dev.hugame.graphics.Texture;
import dev.hugame.vulkan.image.VulkanImageSampler;
import dev.hugame.vulkan.image.VulkanImageView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VulkanTexture implements Texture {
  // TODO: Let this class hold an inner object representing the vulkan
  //       texture and whose type varies depending on the usage
  //       I.e. private final VulkanTextureBase implementation;
  //       That makes this class' implementation only one possible
  //       implementation (static, no re-draw)

  @Getter private final TextureArray textureArray;
  @Getter private final int layer;

  @Deprecated
  public VulkanImageView getImageView() {
    return textureArray.getImageView();
  }

  @Deprecated
  public VulkanImageSampler getImageSampler() {
    return textureArray.getImageSampler();
  }

  @Override
  public int getWidth() {
    return textureArray.getWidth();
  }

  @Override
  public int getHeight() {
    return textureArray.getHeight();
  }
}
