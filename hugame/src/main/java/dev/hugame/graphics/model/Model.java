package dev.hugame.graphics.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL40.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Vector3f;

import dev.hugame.graphics.GLUtils;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.util.Util;

public class Model {

	private static final int POSITION_SIZE = 3;
	private static final int NORMAL_SIZE = 3;
	private static final int TEX_COORDS_SIZE = 2;
	private static final int MAT_ID_SIZE = 1;

	private static final int TRANSFORM_COLUMN_SIZE = 4;
	private static final int TRANSFORM_SIZE = 16;

	private static final int POSITION_OFFSET = 0;
	private static final int NORMAL_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
	private static final int TEX_COORDS_OFFSET = NORMAL_OFFSET + NORMAL_SIZE * Float.BYTES;
	private static final int MAT_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;

	private static final int VERTEX_SIZE = POSITION_SIZE + NORMAL_SIZE + TEX_COORDS_SIZE + MAT_ID_SIZE;
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

	private static final int INSTANCE_SIZE = TRANSFORM_SIZE;
	private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE * Float.BYTES;

	private static final int MAX_INSTANCES = 10000;

	private int vertexAmount;
	private int indexAmount;
	private int vaoID, vboID, i_vboID, eboID;
	private List<Texture> textures;

	public Model(List<AssimpMesh> meshes, List<AssimpMaterial> assimpMaterials) {
		final var vertices = new ArrayList<Float>();
		final var indices = new ArrayList<Integer>();

//		// textures as input to this constructor was removed, so probably
//		// add some support for not using any texture (should be same as 
//		// using white texture I guess)
//		final var materials = assimpMaterials
//				.stream()
//				.map(this::createMaterial)
//				.toList();
//
//		System.out.println("Mesh count: " + meshes.size());
//		System.out.println("Material amount: " + materials.size());
//		
////		meshes.remove(2);
//
//		int indexOffset = 0;
//		for (int i = 0; i < meshes.size(); i++) {
//			final int fIndexOffset = indexOffset;
//			var mesh = meshes.get(i);
//			System.out.println("Adding mesh with index offset " + fIndexOffset);
////			System.out.println("	Mesh:");
//			final var meshVertices = mesh.vertices();
//			final var localIndices = mesh.indices();
//			final var meshIndices = new ArrayList<Integer>();
//			for (int j = 0; j < localIndices.size(); j++) {
//				meshIndices.add(localIndices.get(j) + indexOffset);
//			}
////					.stream()
////					.map(index -> index + fIndexOffset)
////					.toList();
//			final var localMatIndex = mesh.materialIndex();
//			final var globalMatIndex = materials.get(localMatIndex).getIndex();
//			System.out.println("... and global mat index " + globalMatIndex);
////			System.out.println("	Global mat index: " + globalMatIndex);
//			// matIndex is local to mesh, so we need to get the actual material id 
//			// Take in materials, so in here each assimp material in materials
//			// could be mapped to some actual Material and then it could be
//			// submitted to Materials.get and we'd get an ID out which would be
//			// global rather than the local one. So basically we'll use the local
//			// matIndex to index into materials list after input materials have been
//			// mapped properly to real materials and submitted.
//			
//			// TODO: Clean up all classes related to models and Assimp!
//			
//			System.out.println(meshIndices);
//			

		System.out.println("Creating model");

		final var textures = new ArrayList<Texture>();

		// TODO: Make the AssimpMaterial list a list of Material instead,
		// before this constructor is called, so that Model doesn't need
		// to know that Assimp is being used. AssimpMaterial::generate
		// or whatever can be used.
		var materials = assimpMaterials.stream().map(assimpMaterial -> {
			System.out.println("---- MATERIAL");
			var maybeAlbedoMap = assimpMaterial.albedoMap();
			var maybeAlbedoMapIdentity = maybeAddTextureToList(maybeAlbedoMap, textures);
			var albedoMapId = maybeAlbedoMapIdentity.map(TextureIdentity::id).orElse(-1);
			var albedoMapSlice = maybeAlbedoMapIdentity.map(TextureIdentity::slice).orElse(-1);
			System.out.println("Albedo ID: " + albedoMapId + ", albedo slice: " + albedoMapSlice);

			var maybeNormalMap = assimpMaterial.normalMap();
			var maybeNormalMapIdentity = maybeAddTextureToList(maybeNormalMap, textures);
			var normalMapId = maybeNormalMapIdentity.map(TextureIdentity::id).orElse(-1);
			var normalMapSlice = maybeNormalMapIdentity.map(TextureIdentity::slice).orElse(-1);

			var maybeSpecularMap = assimpMaterial.specularMap();
			var maybeSpecularMapIdentity = maybeAddTextureToList(maybeSpecularMap, textures);
			var specularMapId = maybeSpecularMapIdentity.map(TextureIdentity::id).orElse(-1);
			var specularMapSlice = maybeSpecularMapIdentity.map(TextureIdentity::slice).orElse(-1);

			var material = createMaterial(assimpMaterial, albedoMapId, albedoMapSlice, normalMapId, normalMapSlice,
					specularMapId, specularMapSlice);
			return material;
		}).toList();

		var amountOfMaterials = materials.size();

		System.out.println("Total of " + amountOfMaterials + " materials");

		int indexOffset = 0;
		for (int i = 0; i < meshes.size(); i++) {
			System.out.println("Mesh " + i);
			final var mesh = meshes.get(i);
			final var meshIndexOffset = indexOffset;
			final var meshVertices = mesh.vertices();
			final var localIndices = mesh.indices();
			final var globalIndices = localIndices.stream().map(index -> index + meshIndexOffset).toList();

			final var localMatIndex = mesh.materialIndex();
			System.out.println("  Local mat index: " + localMatIndex);
			final var material = materials.get(localMatIndex);
			final var globalMatIndex = material.getIndex();
			System.out.println("  Global mat index: " + globalMatIndex);
			// TODO: Clean up all classes related to models and Assimp!

			for (var vertex : meshVertices) {
				final var position = vertex.position();
				final var normal = vertex.normal();
				final var texCoords = vertex.texCoords();

				vertices.add(position.x);
				vertices.add(position.y);
				vertices.add(position.z);

				vertices.add(normal.x);
				vertices.add(normal.y);
				vertices.add(normal.z);

				vertices.add(texCoords.x);
				vertices.add(texCoords.y);

//				vertices.add(0f); // TODO: Add actual texture ID here
				vertices.add((float) globalMatIndex);
				// Hmm so we got texture2darray working but materials are somehow acting out?
			}
			indices.addAll(globalIndices);
			indexOffset += meshVertices.size();

		}

		setup(vertices, indices, textures);
	}

	public Model(List<Float> vertices, List<Integer> indices, List<Texture> textures) {
		setup(vertices, indices, textures);
	}

	private void setup(List<Float> vertices, List<Integer> indices, List<Texture> textures) {
		System.out.println("Model::setup");
		vertexAmount = vertices.size() / VERTEX_SIZE;
		System.out.println("Vertex amount: " + vertexAmount);
		indexAmount = indices.size();
		System.out.println("Index amount: " + indexAmount);
		this.textures = textures;

		vaoID = GLUtils.createVAO();
		vboID = GLUtils.createStaticVBO(vertices.size() * VERTEX_SIZE_BYTES, Util.toFloatArray(vertices));

		glBindVertexArray(vaoID);

		glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, NORMAL_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, NORMAL_OFFSET);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(2);

		// glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES,
		// TEX_ID_OFFSET);
		// glEnableVertexAttribArray(3);

		glVertexAttribPointer(3, MAT_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, MAT_ID_OFFSET);
		glEnableVertexAttribArray(3);

		i_vboID = GLUtils.createVBO(MAX_INSTANCES * INSTANCE_SIZE_BYTES);

		glVertexAttribPointer(4, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES,
				0 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(4);
		glVertexAttribDivisor(4, 1);

		glVertexAttribPointer(5, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES,
				1 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(5);
		glVertexAttribDivisor(5, 1);

		glVertexAttribPointer(6, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES,
				2 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(6);
		glVertexAttribDivisor(6, 1);

		glVertexAttribPointer(7, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES,
				3 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(7);
		glVertexAttribDivisor(7, 1);

		eboID = createEBO(indices);

		glBindVertexArray(0);
	}

	public int getVAO() {
		return vaoID;
	}

	public int getVBO() {
		return vboID;
	}

	public int getInstaceVBO() {
		return i_vboID;
	}

	public int getEBO() {
		return eboID;
	}

	public int getIndexAmount() {
		return indexAmount;
	}

	public int getVertexAmount() {
		return vertexAmount;
	}

	public List<Texture> getTextures() {
		return textures;
	}

	private Optional<TextureIdentity> maybeAddTextureToList(Optional<Texture> maybeTexture, List<Texture> textures) {
		if (maybeTexture.isPresent()) {
//			System.out.println("-- Adding texture to list");
			var texture = maybeTexture.orElseThrow();
			textures.add(texture);
			var localID = textures.size() - 1;
			return Optional.of(new TextureIdentity(localID, texture.getSlice()));
		} else {
//			System.out.println("-- No texture specified");
			return Optional.empty();
		}
	}

	private Material createMaterial(AssimpMaterial assimpMaterial, int albedoMapID, int albedoMapSlice, int normalMapID,
			int normalMapSlice, int specularMapID, int specularMapSlice) {
		var color = assimpMaterial.albedoColor().orElse(new Vector3f(1));
		return Materials.get(color, color, color, 32, albedoMapID, normalMapID, specularMapID, albedoMapSlice,
				normalMapSlice, specularMapSlice);
	}

	private int createEBO(List<Integer> indexList) {
		int eboID = glGenBuffers();
		var indices = indexList.stream().mapToInt(integer -> integer.intValue()).toArray();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		return eboID;
	}

	private static record TextureIdentity(int id, int slice) {
	}

}
