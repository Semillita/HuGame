package dev.hugame.desktop.gl.shader;

import dev.hugame.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class OpenGLShader implements Shader {
	private int id;
	private String vertexShaderSource;
	private String fragmentShaderSource;

	/** Creates a shader wrapper object of the given shader program ID. */
	public OpenGLShader(int id) {
		this.id = id;
	}

	/** Returns the ID of this shader. */
	public int getID() {
		return id;
	}

	/** Sets this shader as the shader to be used. */
	public void use() {
		glUseProgram(id);
	}

	/** Detaches this shader from being used. */
	public void detach() {
		glUseProgram(0);
	}

	public void uploadMat4f(String varName, Matrix4f mat4) {
		FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
		mat4.get(matBuffer);
		glUniformMatrix4fv(getLocation(varName), false, matBuffer);
	}

	public void uploadFloat(String varName, float val) {
		use();
		glUniform1f(getLocation(varName), val);
	}

	public void uploadInt(String varName, int val) {
		use();
		glUniform1i(getLocation(varName), val);
	}

	public void uploadTexture(String varName, int slot) {
		use();
		glUniform1i(getLocation(varName), slot);
	}

	public void uploadTextureArray(String varName, int[] slots) {
		use();
		glUniform1iv(getLocation(varName), slots);
	}

	public void uploadVec3(String varName, Vector3f var) {
		use();
		glUniform3f(getLocation(varName), var.x, var.y, var.z);
	}

	private int getLocation(String varName) {
		return glGetUniformLocation(id, varName);
	}
}
