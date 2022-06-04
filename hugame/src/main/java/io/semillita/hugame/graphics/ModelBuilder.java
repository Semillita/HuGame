package io.semillita.hugame.graphics;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

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

	// Ta in texture, tex coords och 12 (-_-) floats för position av vertices
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

	public void triangle(Vector3f vert1, Vector3f vert2, Vector3f vert3, float u1, float v1,
			float u2, float v2, float u3, float v3, int texID) {

		vertices.add(vert1.x);
		vertices.add(vert1.y);
		vertices.add(vert1.z);

		vertices.add(1f);
		vertices.add(1f);
		vertices.add(1f);
		vertices.add(1f);

		vertices.add(u1);
		vertices.add(v1);

		vertices.add((float) texID);
		
		vertices.add(vert2.x);
		vertices.add(vert2.y);
		vertices.add(vert2.z);

		vertices.add(1f);
		vertices.add(1f);
		vertices.add(1f);
		vertices.add(1f);
		
		vertices.add(u2);
		vertices.add(v2);

		vertices.add((float) texID);
		
		vertices.add(vert3.x);
		vertices.add(vert3.y);
		vertices.add(vert3.z);

		vertices.add(1f);
		vertices.add(1f);
		vertices.add(1f);
		vertices.add(1f);

		vertices.add(u3);
		vertices.add(v3);
		
		vertices.add((float) texID);
		
		indices.add(idx + 0);
		indices.add(idx + 1);
		indices.add(idx + 2);

		idx += 3;
	}
	
	public void texture(Texture texture) {
		textures.add(texture);
	}

	public Model generate() {
		Model model = new Model(vertices, indices, textures);
		vertices.clear();
		indices.clear();
		textures = new ArrayList<>();
		idx = 0;
		return model;
	}

}
