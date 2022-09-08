package dev.hugame.graphics;

import java.util.Optional;

import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL40.*;

public class Shaders {

	public static Optional<Shader> get(String vertexSource, String fragmentSource) {
		var vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShaderID, vertexSource);
		glCompileShader(vertexShaderID);
		var vertexSuccess = glGetShaderi(vertexShaderID, GL_COMPILE_STATUS);
		
		if (vertexSuccess == GL_FALSE) {
			var len = glGetShaderi(vertexShaderID, GL_INFO_LOG_LENGTH);
			System.err.println("ERROR: Vertex shader compilation failed");
			System.err.println(glGetShaderInfoLog(vertexShaderID, len));
			System.err.println(vertexSource);
			return Optional.empty();
		}
		
		var fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShaderID, fragmentSource);
		glCompileShader(fragmentShaderID);
		var fragmentSuccess = glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS);
		
		if (fragmentSuccess == GL_FALSE) {
			var len = glGetShaderi(fragmentShaderID, GL_INFO_LOG_LENGTH);
			System.err.println("ERROR: Fragment shader compilation failed");
			System.err.println(glGetShaderInfoLog(fragmentShaderID, len));
			System.err.println(fragmentSource);
			return Optional.empty();
		}
		
		var shaderProgramID = glCreateProgram();
		glAttachShader(shaderProgramID, vertexShaderID);
		glAttachShader(shaderProgramID, fragmentShaderID);
		glLinkProgram(shaderProgramID);
		var programSuccess = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
		
		if (programSuccess == GL_FALSE) {
			var len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
			System.err.println("ERROR: Linking of shaders failed");
			System.err.println(glGetProgramInfoLog(shaderProgramID, len));
			return Optional.empty();
		}
		
		return Optional.of(new Shader(shaderProgramID));
		
	}

}
