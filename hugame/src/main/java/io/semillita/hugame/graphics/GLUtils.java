package io.semillita.hugame.graphics;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.List;

import org.joml.Matrix4f;

public class GLUtils {

	public static int createVAO() {
		var vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		return vaoID;
	}
	
	public static int createVBO(int sizeInBytes) {
		var vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, sizeInBytes, GL_DYNAMIC_DRAW);
		return vboID;
	}
	
	public static int createStaticVBO(int sizeInBytes, float[] values) {
		var vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, values, GL_STATIC_DRAW);
		return vboID;
	}
	
	public static void fillVBO(int vboID, List<Float> values) {
		float[] vertArr = new float[values.size()];
		for (int i = 0; i < values.size(); i++) {
			vertArr[i] = values.get(i);
		}
		
		fillVBO(vboID, vertArr);
	}
	
	public static void fillVBO(int vboID, float[] values) {
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferSubData(GL_ARRAY_BUFFER, 0, values);
	}
	
	public static void uploadMatricesToShader(Camera camera, Shader shader) {
		shader.uploadMat4f("uProjection", camera.getProjectionMatrix());
		shader.uploadMat4f("uView", camera.getViewMatrix());
	}
	
}
