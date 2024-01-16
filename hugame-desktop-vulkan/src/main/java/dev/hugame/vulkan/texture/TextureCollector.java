package dev.hugame.vulkan.texture;

import dev.hugame.graphics.ResolvedTexture;
import dev.hugame.vulkan.core.VulkanGraphics;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TextureCollector {
  private final Map<Dimension, TextureArraySpec> texturesBySize = new HashMap<>();

  public VulkanTexture addTexture(ResolvedTexture resolvedTexture) {
    var width = resolvedTexture.width();
    var height = resolvedTexture.height();
    var dimensions = new Dimension(width, height);

    var textureArrayData =
        Optional.ofNullable(texturesBySize.get(dimensions))
            .orElseGet(
                () -> {
                  var textureArray = new TextureArray();
                  var textureArraySpec =
                      new TextureArraySpec(textureArray, width, height, new ArrayList<>());
                  texturesBySize.put(dimensions, textureArraySpec);
                  return textureArraySpec;
                });

    var images = textureArrayData.images();
    var layer = images.size();
    images.add(resolvedTexture);

    return new VulkanTexture(textureArrayData.textureArray(), layer);
  }

  public void generate(VulkanGraphics graphics) {
    System.out.println("Generating textures");
    System.out.println(texturesBySize);
    texturesBySize.forEach(
        (dimension, textureArraySpec) -> {
          textureArraySpec.textureArray.initialize(
              graphics, textureArraySpec.images, textureArraySpec.width, textureArraySpec.height);
        });
  }

  private record TextureArraySpec(
      TextureArray textureArray, int width, int height, List<ResolvedTexture> images) {}
}
