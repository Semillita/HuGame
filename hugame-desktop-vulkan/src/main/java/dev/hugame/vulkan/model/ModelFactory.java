package dev.hugame.vulkan.model;

import dev.hugame.model.spec.ResolvedMesh;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.core.VertexInput;
import dev.hugame.vulkan.core.VulkanGraphics;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ModelFactory {
	public VulkanModel create(VulkanGraphics graphics, ResolvedModel resolvedModel) {
		var resolvedMeshes = resolvedModel.getMeshes();

		var vertexCount = resolvedMeshes
				.stream()
				.map(ResolvedMesh::getVertices)
				.mapToInt(List::size)
				.reduce(0, Integer::sum);
		var vertices = new float[vertexCount * VertexInput.VERTEX_SIZE];

		var indexCount = resolvedMeshes
				.stream()
				.map(ResolvedMesh::getIndices)
				.mapToInt(List::size)
				.reduce(0, Integer::sum);
		var indices = new int[indexCount];

		int vertexOffset = 0, indexOffset = 0;
		for (var mesh : resolvedMeshes) {
			var meshVertices = mesh.getVertices();
			var meshIndices = mesh.getIndices();

			for (var meshIndex : meshIndices) {
				indices[indexOffset++] = vertexOffset + meshIndex;
			}

			// TODO: Make this process depend on a certain vertex content config
			for (var vertex : meshVertices) {
				var position = vertex.getPosition();
				var normal = vertex.getNormal();
				var texCoords = vertex.getTextureCoordinates();

				var vertexValueOffset = vertexOffset * 8; // Times the float count per vertex.

				vertices[vertexValueOffset++] = position.x;
				vertices[vertexValueOffset++] = position.y;
				vertices[vertexValueOffset++] = position.z;

				// For now, use color. Later use normal here instead.
				vertices[vertexValueOffset++] = 1f;
				vertices[vertexValueOffset++] = 0f;
				vertices[vertexValueOffset++] = 0f;

				vertices[vertexValueOffset++] = texCoords.x;
				vertices[vertexValueOffset] = texCoords.y;

				vertexOffset++;
			}
		}

		var vertexBuffer = VulkanVertexBuffer.create(graphics, vertices);
		var instanceBuffer = VulkanVertexBuffer.create(graphics, 10_000 * 16 * Float.BYTES);
		var indexBuffer = VulkanIndexBuffer.create(graphics, indices);

		return new VulkanModel(vertexBuffer, instanceBuffer, indexBuffer);
	}
}
