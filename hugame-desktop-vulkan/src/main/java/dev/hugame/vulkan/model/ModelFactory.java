package dev.hugame.vulkan.model;

import dev.hugame.graphics.Texture;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.model.spec.ResolvedMaterial;
import dev.hugame.model.spec.ResolvedMesh;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.implementation.DefaultModelPipelineDescriptors;
import dev.hugame.vulkan.texture.TextureArray;
import dev.hugame.vulkan.texture.VulkanTexture;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.joml.Vector3f;

public class ModelFactory {
  private static int i = 0;

  public VulkanModel create(VulkanGraphics graphics, ResolvedModel resolvedModel) {
    var resolvedMeshes = resolvedModel.getMeshes();
    var resolvedMaterials = resolvedModel.getMaterials();

    var vertexCount =
        resolvedMeshes.stream()
            .map(ResolvedMesh::getVertices)
            .mapToInt(List::size)
            .reduce(0, Integer::sum);
    var vertices = new float[vertexCount * DefaultModelPipelineDescriptors.VERTEX_SIZE];
    var verticesByteBuffer =
        ByteBuffer.allocate(vertexCount * DefaultModelPipelineDescriptors.VERTEX_SIZE * 4);

    var textures = new ArrayList<Texture>();
    var textureArrays = new ArrayList<TextureArray>();

    var materials =
        resolvedMaterials.stream()
            .map(
                resolvedMaterial -> {
                  var maybeAlbedoMap =
                      resolvedMaterial.getAlbedoMap().map(VulkanTexture.class::cast);
                  var maybeAlbedoMapIdentity = maybeAddTextureToList(maybeAlbedoMap, textureArrays);
                  var albedoMapIndex =
                      maybeAlbedoMapIdentity.map(TextureIdentity::index).orElse(-1);
                  var albedoMapLayer =
                      maybeAlbedoMapIdentity.map(TextureIdentity::layer).orElse(-1);

                  var maybeNormalMap =
                      resolvedMaterial.getNormalMap().map(VulkanTexture.class::cast);
                  var maybeNormalMapIdentity = maybeAddTextureToList(maybeNormalMap, textureArrays);
                  var normalMapIndex =
                      maybeNormalMapIdentity.map(TextureIdentity::index).orElse(-1);
                  var normalMapLayer =
                      maybeNormalMapIdentity.map(TextureIdentity::layer).orElse(-1);

                  var maybeSpecularMap =
                      resolvedMaterial.getSpecularMap().map(VulkanTexture.class::cast);
                  var maybeSpecularMapIdentity =
                      maybeAddTextureToList(maybeSpecularMap, textureArrays);
                  var specularMapIndex =
                      maybeSpecularMapIdentity.map(TextureIdentity::index).orElse(-1);
                  var specularMapLayer =
                      maybeSpecularMapIdentity.map(TextureIdentity::layer).orElse(-1);

                  var actualMaterial =
                      createMaterial(
                          resolvedMaterial,
                          albedoMapIndex,
                          albedoMapLayer,
                          normalMapIndex,
                          normalMapLayer,
                          specularMapIndex,
                          specularMapLayer);
                  return actualMaterial;
                })
            .toList();

    var indexCount =
        resolvedMeshes.stream()
            .map(ResolvedMesh::getIndices)
            .mapToInt(List::size)
            .reduce(0, Integer::sum);
    var indices = new int[indexCount];
    int vertexOffset = 0, indexOffset = 0;

    for (var mesh : resolvedMeshes) {
      final var meshVertices = mesh.getVertices();
      final var meshIndices = mesh.getIndices();

      final var meshMaterialIndex = mesh.getMaterialIndex();
      final var material = materials.get(meshMaterialIndex);
      final var globalMaterialIndex = material.getIndex();

      for (var meshIndex : meshIndices) {
        indices[indexOffset++] = vertexOffset + meshIndex;
      }

      // TODO: Make this process depend on a certain vertex content config
      for (var vertex : meshVertices) {
        var position = vertex.getPosition();
        var normal = vertex.getNormal();
        var texCoords = vertex.getTextureCoordinates();

        var vertexValueOffset = vertexOffset * 9; // Times the float count per vertex.

        verticesByteBuffer.rewind();
        verticesByteBuffer.putFloat(position.x);
        verticesByteBuffer.putFloat(position.y);
        verticesByteBuffer.putFloat(position.z);

        verticesByteBuffer.putFloat(normal.x);
        verticesByteBuffer.putFloat(normal.y);
        verticesByteBuffer.putFloat(normal.z);

        verticesByteBuffer.putFloat(texCoords.x);
        verticesByteBuffer.putFloat(texCoords.y);

        verticesByteBuffer.putInt(globalMaterialIndex);

        vertices[vertexValueOffset++] = position.x;
        vertices[vertexValueOffset++] = position.y;
        vertices[vertexValueOffset++] = position.z;

        vertices[vertexValueOffset++] = normal.x;
        vertices[vertexValueOffset++] = normal.y;

        vertices[vertexValueOffset++] = normal.z;

        vertices[vertexValueOffset++] = texCoords.x;
        vertices[vertexValueOffset++] = texCoords.y;

        vertices[vertexValueOffset] = Float.intBitsToFloat(globalMaterialIndex);

        vertexOffset++;
      }
    }

    /*var textures =
    resolvedModel.getMaterials().stream()
        .flatMap(
            resolvedMaterial ->
                Stream.of(
                        resolvedMaterial.getAlbedoMap(),
                        resolvedMaterial.getNormalMap(),
                        resolvedMaterial.getSpecularMap())
                    .flatMap(Optional::stream))
        .map(VulkanTexture.class::cast)
        .toList();*/

    var vertexBuffer = VulkanVertexBuffer.create(graphics, vertices);
    var instanceBuffer = VulkanVertexBuffer.create(graphics, 10_000 * 16 * Float.BYTES);
    var indexBuffer = VulkanIndexBuffer.create(graphics, indices);

    return new VulkanModel(vertexBuffer, instanceBuffer, indexBuffer, textures, textureArrays);
  }

  private static Material createMaterial(
      ResolvedMaterial resolvedMaterial,
      int albedoMapIndex,
      int albedoMapLayer,
      int normalMapIndex,
      int normalMapLayer,
      int specularMapIndex,
      int specularMapLayer) {
    var color = resolvedMaterial.getAlbedoColor().orElse(new Vector3f(1));
    return Materials.get(
        color,
        color,
        color,
        32,
        albedoMapIndex,
        normalMapIndex,
        specularMapIndex,
        albedoMapLayer,
        normalMapLayer,
        specularMapLayer);
  }

  private static Optional<TextureIdentity> maybeAddTextureToList(
      Optional<VulkanTexture> maybeTexture, List<TextureArray> textureArrays) {
    return maybeTexture.map(
        texture -> {
          var textureArray = texture.getTextureArray();
          if (textureArrays.contains(textureArray)) {
            return new TextureIdentity(textureArrays.indexOf(textureArray), texture.getLayer());
          }

          var localIndex = textureArrays.size();
          textureArrays.add(textureArray);
          return new TextureIdentity(localIndex, texture.getLayer());
        });
  }

  private record TextureIdentity(int index, int layer) {}
}
