package dev.hugame.graphics;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import dev.hugame.core.graphics.Model;

/** Utility class for defining models through code. */
public class ModelBuilder {

	private List<Texture> textures;
	private List<Float> vertices;
	private List<Integer> indices;

	private int idx = 0;

	public ModelBuilder() {
		textures = new ArrayList<>();
		vertices = new ArrayList<>();
		indices = new ArrayList<>();
	}

	/**
	 * Generates a cube model centered at (0, 0, 0) with the dimensions (1, 1, 1)
	 * units, with the given textures.
	 * 
	 * @param t0 the top side texture
	 * @param t1 the bottom side texture
	 * @param t2 the front side texture
	 * @param t3 the back side texture
	 * @param t4 the left side texture
	 * @param t5 the right side texture
	 */
	public void cube(Texture t0, Texture t1, Texture t2, Texture t3, Texture t4, Texture t5) {

		textures.add(t0);
		textures.add(t1);
		textures.add(t2);
		textures.add(t3);
		textures.add(t4);
		textures.add(t5);

		Vector3f[] vertices = { new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0.5f, 0.5f, -0.5f),
				new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(-0.5f, -0.5f, -0.5f),
				new Vector3f(0.5f, -0.5f, -0.5f), new Vector3f(0.5f, -0.5f, 0.5f), new Vector3f(-0.5f, -0.5f, 0.5f) };

		// TOP
		triangle(vertices[0], vertices[1], vertices[2], 0, 0, 1, 0, 1, 1, 0);
		triangle(vertices[2], vertices[3], vertices[0], 1, 1, 0, 1, 0, 0, 0);

		// BOTTOM
		triangle(vertices[4], vertices[5], vertices[6], 0, 0, 1, 0, 1, 1, 1);
		triangle(vertices[6], vertices[7], vertices[4], 1, 1, 0, 1, 0, 0, 1);

		// FRONT
		triangle(vertices[3], vertices[2], vertices[6], 0, 0, 1, 0, 1, 1, 2);
		triangle(vertices[6], vertices[7], vertices[3], 1, 1, 0, 1, 0, 0, 2);

		// BACK
		triangle(vertices[0], vertices[1], vertices[5], 0, 0, 1, 0, 1, 1, 3);
		triangle(vertices[5], vertices[4], vertices[0], 1, 1, 0, 1, 0, 0, 3);

		// LEFT
		triangle(vertices[0], vertices[3], vertices[7], 0, 0, 1, 0, 1, 1, 4);
		triangle(vertices[7], vertices[4], vertices[0], 1, 1, 0, 1, 0, 0, 4);

		// RIGHT
		triangle(vertices[2], vertices[1], vertices[5], 0, 0, 1, 0, 1, 1, 5);
		triangle(vertices[5], vertices[6], vertices[2], 1, 1, 0, 1, 0, 0, 5);
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
	 * @param texID the index of the texture in this builder's texture list
	 */
	public void triangle(Vector3f vert1, Vector3f vert2, Vector3f vert3, float u1, float v1, float u2, float v2,
			float u3, float v3, int texID) {

		var a = new Vector3f(vert1).sub(vert2);
		var b = new Vector3f(vert3).sub(vert2);
		var normal = a.cross(b);

		// Position 0
		vertices.add(vert1.x);
		vertices.add(vert1.y);
		vertices.add(vert1.z);

		// Normal 0
		vertices.add(normal.x);
		vertices.add(normal.y);
		vertices.add(normal.z);

		// UV 0
		vertices.add(u1);
		vertices.add(v1);

		// Tex ID 0
		vertices.add((float) texID);

		// Position 1
		vertices.add(vert2.x);
		vertices.add(vert2.y);
		vertices.add(vert2.z);

		// Normal 1
		vertices.add(normal.x);
		vertices.add(normal.y);
		vertices.add(normal.z);

		// UV 1
		vertices.add(u2);
		vertices.add(v2);

		// Tex ID 1
		vertices.add((float) texID);

		// Position 2
		vertices.add(vert3.x);
		vertices.add(vert3.y);
		vertices.add(vert3.z);

		// Normal 2
		vertices.add(normal.x);
		vertices.add(normal.y);
		vertices.add(normal.z);

		// UV 2
		vertices.add(u3);
		vertices.add(v3);

		// Tex ID 2
		vertices.add((float) texID);

		indices.add(idx + 0);
		indices.add(idx + 1);
		indices.add(idx + 2);

		idx += 3;
	}

	/**
	 * Adds a texture to this builder's texture list such that it can be used by its
	 * index as texID in {@link ModelBuilder#triangle}.
	 */
	public void texture(Texture texture) {
		textures.add(texture);
	}

	/**
	 * Generates a model out of the supplied vertices and textures.
	 * 
	 * @return the model
	 */
	public Model generate() {
		Model model = new Model(vertices, indices, textures);
		vertices.clear();
		indices.clear();
		textures = new ArrayList<>();
		idx = 0;
		return model;
	}

}
