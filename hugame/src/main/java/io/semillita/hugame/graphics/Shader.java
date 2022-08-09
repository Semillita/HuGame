package io.semillita.hugame.graphics;

import static org.lwjgl.opengl.GL40.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import io.semillita.hugame.util.Files;

public class Shader {

	private int id;
	private String vertexShaderSource;
	private String fragmentShaderSource;

	public Shader(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public void use() {
		glUseProgram(id);
	}
	
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
	
	public void uploadTexture(String varName, int slot) {
		use();
		glUniform1i(getLocation(varName), slot);
	}
	
	public void uploadTextureArray(String varName, int[] slots) {
		use();
		glUniform1iv(getLocation(varName), slots);
	}
	
	private int getLocation(String varName) {
		return glGetUniformLocation(id, varName);
	}
}
