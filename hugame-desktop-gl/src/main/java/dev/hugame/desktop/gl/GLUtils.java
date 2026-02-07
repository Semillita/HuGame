package dev.hugame.desktop.gl;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

import dev.hugame.desktop.gl.buffer.UniformBuffer;
import dev.hugame.desktop.gl.shader.OpenGLShader;
import dev.hugame.graphics.Camera;
import dev.hugame.graphics.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

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

    public static int createStaticVBO(int sizeInBytes, ByteBuffer values) {
        var vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, values, GL_STATIC_DRAW);
        return vboID;
    }

    public static void fillVBO(int vboID, ByteBuffer vertexData) {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        vertexData.rewind();
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData);
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

	public static void uploadMatricesToShader(Camera camera, OpenGLShader shader) {
		shader.uploadMat4f("uProjection", camera.getProjectionMatrix());
		shader.uploadMat4f("uView", camera.getViewMatrix());
	}

    public static void uploadToUniformBuffer(UniformBuffer uniformBuffer, Consumer<ByteBuffer> callback) {
        var nativeBuffer = MemoryUtil.memAlloc(uniformBuffer.getSize());
        callback.accept(nativeBuffer);
        nativeBuffer.rewind();

        uniformBuffer.bind();
        uniformBuffer.bufferSubData(nativeBuffer);

        MemoryUtil.memFree(nativeBuffer);
    }

    /*public static void uploadMatricesToUniformBuffer(Camera camera, UniformBuffer uniformBuffer) {
        var dataBuffer = MemoryUtil.memAlloc(Float.BYTES * 16 * 2);

        camera.getProjectionMatrix().get(dataBuffer);
        camera.getViewMatrix().get(Float.BYTES * 16, dataBuffer);

        dataBuffer.flip();

        uniformBuffer.bind();
        uniformBuffer.bufferSubData(dataBuffer);

        MemoryUtil.memFree(dataBuffer);
    }*/

}
