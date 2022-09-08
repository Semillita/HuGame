package dev.hugame.graphics;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL40.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import dev.hugame.util.Transform;
import dev.hugame.util.Util;

public class Model {
	
	private static final int POSITION_SIZE = 3;
	private static final int NORMAL_SIZE = 3;
	private static final int TEX_COORDS_SIZE = 2;
	private static final int TEX_ID_SIZE = 1;
	
	private static final int TRANSFORM_COLUMN_SIZE = 4;
	private static final int TRANSFORM_SIZE = 16;

	private static final int POSITION_OFFSET = 0;
	private static final int NORMAL_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
	private static final int TEX_COORDS_OFFSET = NORMAL_OFFSET + NORMAL_SIZE * Float.BYTES;
	private static final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;

	private static final int VERTEX_SIZE = POSITION_SIZE + NORMAL_SIZE + TEX_COORDS_SIZE + TEX_ID_SIZE;
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;
	
	private static final int INSTANCE_SIZE = TRANSFORM_SIZE + 1;
	private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE * Float.BYTES;
	
	private static final int MAX_INSTANCES = 10000;
	
	private final int vertexAmount, indexAmount;
	private int vaoID, vboID, i_vboID, eboID;
	private List<Texture> textures;
	
	public Model(List<Float> vertices, List<Integer> indices, List<Texture> textures) {
		vertexAmount = vertices.size() / VERTEX_SIZE;
		indexAmount = indices.size();
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
		
		glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
		glEnableVertexAttribArray(3);
		
		i_vboID = GLUtils.createVBO(MAX_INSTANCES * INSTANCE_SIZE_BYTES);
		
		glVertexAttribPointer(4, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES, 0 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(4);
		glVertexAttribDivisor(4, 1);
		
		glVertexAttribPointer(5, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES, 1 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(5);
		glVertexAttribDivisor(5, 1);
		
		glVertexAttribPointer(6, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES, 2 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(6);
		glVertexAttribDivisor(6, 1);
		
		glVertexAttribPointer(7, TRANSFORM_COLUMN_SIZE, GL_FLOAT, false, INSTANCE_SIZE_BYTES, 3 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(7);
		glVertexAttribDivisor(7, 1);
		
		glVertexAttribPointer(8, 1, 					GL_FLOAT,   false, INSTANCE_SIZE_BYTES, 4 * TRANSFORM_COLUMN_SIZE * Float.BYTES);
		glEnableVertexAttribArray(8);
		glVertexAttribDivisor(8, 1);
		
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
	
	private int createEBO(List<Integer> indexList) {
		int eboID = glGenBuffers();
		var indices = indexList.stream().mapToInt(integer -> integer.intValue()).toArray();
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		return eboID;
	}
	
}
