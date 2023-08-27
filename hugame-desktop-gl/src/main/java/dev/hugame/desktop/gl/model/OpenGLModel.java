package dev.hugame.desktop.gl.model;

import dev.hugame.desktop.gl.GLUtils;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.model.spec.ResolvedMaterial;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.Util;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL45.*;

public class OpenGLModel implements Model {
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

	private static final int MAX_INSTANCES = 10_000;

	private int vertexAmount;
	private int indexAmount;
	private int vaoID, vboID, i_vboID, eboID;
	private List<Texture> textures;

	public static Model from(ResolvedModel resolvedModel) {
		final var vertices = new ArrayList<Float>();
		final var indices = new ArrayList<Integer>();

		final var textures = new ArrayList<Texture>();

		var resolvedMaterials = resolvedModel.getMaterials();
		var resolvedMeshes = resolvedModel.getMeshes();

		// before this constructor is called, so that Model doesn't need
		// to know that Assimp is being used. AssimpMaterial::generate
		// or whatever can be used.
		var materials = resolvedMaterials.stream().map(resolvedMaterial -> {
			System.out.println("---- MATERIAL");
			var maybeAlbedoMap = resolvedMaterial.getAlbedoMap();
			var maybeAlbedoMapIdentity = maybeAddTextureToList(maybeAlbedoMap, textures);
			var albedoMapId = maybeAlbedoMapIdentity.map(TextureIdentity::id).orElse(-1);
			var albedoMapSlice = maybeAlbedoMapIdentity.map(TextureIdentity::slice).orElse(-1);
			System.out.println("Albedo ID: " + albedoMapId + ", albedo slice: " + albedoMapSlice);

			var maybeNormalMap = resolvedMaterial.getNormalMap();
			var maybeNormalMapIdentity = maybeAddTextureToList(maybeNormalMap, textures);
			var normalMapId = maybeNormalMapIdentity.map(TextureIdentity::id).orElse(-1);
			var normalMapSlice = maybeNormalMapIdentity.map(TextureIdentity::slice).orElse(-1);

			var maybeSpecularMap = resolvedMaterial.getSpecularMap();
			var maybeSpecularMapIdentity = maybeAddTextureToList(maybeSpecularMap, textures);
			var specularMapId = maybeSpecularMapIdentity.map(TextureIdentity::id).orElse(-1);
			var specularMapSlice = maybeSpecularMapIdentity.map(TextureIdentity::slice).orElse(-1);

			var actualMaterial = createMaterial(resolvedMaterial, albedoMapId, albedoMapSlice, normalMapId, normalMapSlice,
					specularMapId, specularMapSlice);
			return actualMaterial;
		}).toList();

		int indexOffset = 0;
		for (int i = 0; i < resolvedMeshes.size(); i++) {
			final var mesh = resolvedMeshes.get(i);
			final var meshIndexOffset = indexOffset;
			final var meshVertices = mesh.getVertices();
			final var localIndices = mesh.getIndices();
			final var globalIndices = localIndices.stream().map(index -> index + meshIndexOffset).toList();

			final var localMatIndex = mesh.getMaterialIndex();
			final var material = materials.get(localMatIndex);
			final var globalMatIndex = material.getIndex();

			for (var vertex : meshVertices) {
				final var position = vertex.getPosition();
				final var normal = vertex.getNormal();
				final var texCoords = vertex.getTextureCoordinates();

				vertices.add(position.x);
				vertices.add(position.y);
				vertices.add(position.z);

				vertices.add(normal.x);
				vertices.add(normal.y);
				vertices.add(normal.z);

				vertices.add(texCoords.x);
				vertices.add(texCoords.y);

				vertices.add((float) globalMatIndex);
			}
			indices.addAll(globalIndices);
			indexOffset += meshVertices.size();

		}

		var model = new OpenGLModel();

		model.setup(vertices, indices, textures);

		return model;
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

	public int getIndexCount() {
		return indexAmount;
	}

	public int getVertexCount() {
		return vertexAmount;
	}

	public List<Texture> getTextures() {
		return textures;
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

	private static Optional<TextureIdentity> maybeAddTextureToList(Optional<Texture> maybeTexture, List<Texture> textures) {
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

	private static Material createMaterial(ResolvedMaterial resolvedMaterial, int albedoMapID, int albedoMapSlice, int normalMapID,
									int normalMapSlice, int specularMapID, int specularMapSlice) {
		var color = resolvedMaterial.getAlbedoColor().orElse(new Vector3f(1));
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
