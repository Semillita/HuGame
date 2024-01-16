package dev.hugame.graphics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.hugame.graphics.material.Material;
import dev.hugame.model.spec.ResolvedMaterial;
import dev.hugame.model.spec.ResolvedMesh;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.model.spec.ResolvedVertex;
import dev.hugame.util.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;

import dev.hugame.graphics.Texture;

/** Utility class for defining models through code. */
public class ModelBuilder {
	private final List<ResolvedMesh> meshes = new ArrayList<>();
	private final List<ResolvedMaterial> materials = new ArrayList<>();

	/**
	 * Generates a cube model centered at (0, 0, 0) with the dimensions (1, 1, 1)
	 * units, with the given texture.
	 * 
	 * @param texture the texture to use for each side
	 */
	public ModelBuilder cube(Texture texture) {
		var material = new ResolvedMaterial(Optional.empty(), Optional.of(texture), Optional.empty(), Optional.empty());

		var vertices = new ArrayList<ResolvedVertex>();
		var indices = new ArrayList<Integer>();

		var positions = new Vector3f[] { new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.5f, 0.5f, -0.5f),
				new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(-0.5f, -0.5f, -0.5f),
				new Vector3f(0.5f, -0.5f, -0.5f), new Vector3f(0.5f, -0.5f, 0.5f), new Vector3f(-0.5f, -0.5f, 0.5f) };

		// TOP
		createTriangle(positions[0], positions[1], positions[2], 0, 0, 1, 0, 1, 1, vertices, indices);
		createTriangle(positions[2], positions[3], positions[0], 1, 1, 0, 1, 0, 0, vertices, indices);

		// BOTTOM
		createTriangle(positions[4], positions[5], positions[6], 0, 0, 1, 0, 1, 1, vertices, indices);
		createTriangle(positions[6], positions[7], positions[4], 1, 1, 0, 1, 0, 0, vertices, indices);

		// FRONT
		createTriangle(positions[3], positions[2], positions[6], 0, 0, 1, 0, 1, 1, vertices, indices);
		createTriangle(positions[6], positions[7], positions[3], 1, 1, 0, 1, 0, 0, vertices, indices);

		// BACK
		createTriangle(positions[0], positions[1], positions[5], 0, 0, 1, 0, 1, 1, vertices, indices);
		createTriangle(positions[5], positions[4], positions[0], 1, 1, 0, 1, 0, 0, vertices, indices);

		// LEFT
		createTriangle(positions[0], positions[3], positions[7], 0, 0, 1, 0, 1, 1, vertices, indices);
		createTriangle(positions[7], positions[4], positions[0], 1, 1, 0, 1, 0, 0, vertices, indices);

		// RIGHT
		createTriangle(positions[2], positions[1], positions[5], 0, 0, 1, 0, 1, 1, vertices, indices);
		createTriangle(positions[5], positions[6], positions[2], 1, 1, 0, 1, 0, 0, vertices, indices);

		var mesh = new ResolvedMesh(vertices, indices, materials.size());
		meshes.add(mesh);
		materials.add(material);

		return this;
	}

	public ModelBuilder plane(Texture texture) {
		var material = new ResolvedMaterial(Optional.empty(), Optional.of(texture), Optional.empty(),Optional.empty());

		var vertices = new ArrayList<ResolvedVertex>();
		var indices = new ArrayList<Integer>();

		var positions = new Vector3f[] {
				new Vector3f(-0.5f, 0, 0.5f),
				new Vector3f(-0.5f, 0, -0.5f),
				new Vector3f(0.5f, 0, -0.5f),
				new Vector3f(0.5f, 0, 0.5f) };

		createTriangle(positions[0], positions[2], positions[1], 0, 0, 1, 1, 0, 1, vertices, indices);
		createTriangle(positions[0], positions[3], positions[2], 0, 0, 1, 0, 1, 1, vertices, indices);

		meshes.add(new ResolvedMesh(vertices, indices, materials.size()));
		materials.add(material);

		return this;
	}

	/**
	 * Generates a triangle at the given position with the given texture
	 * coordinates.
	 * 
	 * @param vert1 the position of the first vertex
	 * @param vert2 the position of the second vertex
	 * @param vert3 the position of the third vertex
	 * @param u1    the u-coordiate of the first vertex
	 * @param v1    the v-coordinate of the first vertex
	 * @param u2    the u-coordinate of the second vertex
	 * @param v2    the v-coordinate of the second vertex
	 * @param u3    the u-coordinate of the third vertex
	 * @param v3    the v-coordinate the third vertex
	 */
	private void createTriangle(Vector3f vert1, Vector3f vert2, Vector3f vert3, float u1, float v1, float u2, float v2,
								float u3, float v3, List<ResolvedVertex> vertices, List<Integer> indices) {
		var a = new Vector3f(vert2).sub(vert1);
		var b = new Vector3f(vert3).sub(vert1);
		var normal = a.cross(b);

		var vertexCount = vertices.size();

		vertices.add(new ResolvedVertex(vert1, normal, new Vector2f(u1, v1)));
		vertices.add(new ResolvedVertex(vert2, normal, new Vector2f(u2, v2)));
		vertices.add(new ResolvedVertex(vert3, normal, new Vector2f(u3, v3)));

		indices.add(vertexCount);
		indices.add(vertexCount + 1);
		indices.add(vertexCount + 2);
	}

	/**
	 * Generates a model out of the supplied vertices and textures.
	 * 
	 * @return the model
	 */
	public ResolvedModel generate() {
		return new ResolvedModel(meshes, materials);
	}

}
