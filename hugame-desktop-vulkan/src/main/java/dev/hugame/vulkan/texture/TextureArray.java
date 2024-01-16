package dev.hugame.vulkan.texture;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;

import dev.hugame.graphics.ResolvedTexture;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.image.ImageUtils;
import dev.hugame.vulkan.image.VulkanImage;
import dev.hugame.vulkan.image.VulkanImageSampler;
import dev.hugame.vulkan.image.VulkanImageView;
import dev.hugame.vulkan.types.ImageType;
import dev.hugame.vulkan.types.ImageViewType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TextureArray {
  @Getter private int width = 0;
  @Getter private int height = 0;
  @Getter private int layerCount = 0;
  @Getter private VulkanImage image = null;
  @Getter private VulkanImageView imageView = null;
  @Getter private VulkanImageSampler imageSampler = null;
  private boolean initialized = false;

  public void initialize(
      VulkanGraphics graphics, List<ResolvedTexture> resolvedTextures, int width, int height) {
    if (initialized) {
      throw new RuntimeException();
    }
    initialized = true;

    this.width = width;
    this.height = height;
    this.layerCount = resolvedTextures.size();

    this.image =
        ImageUtils.createImage(
            graphics, resolvedTextures, width, height, VK_IMAGE_ASPECT_COLOR_BIT, ImageType._2D);
    this.imageView =
        VulkanImageView.create(
            graphics,
            image,
            ImageViewType._2D_ARRAY,
            layerCount,
            VK_FORMAT_R8G8B8A8_SRGB,
            VK_IMAGE_ASPECT_COLOR_BIT);
    this.imageSampler = VulkanImageSampler.create(graphics);
  }
}
