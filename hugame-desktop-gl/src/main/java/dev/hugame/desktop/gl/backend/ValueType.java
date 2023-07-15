package dev.hugame.desktop.gl.backend;

import static org.lwjgl.opengl.GL45.*;

public enum ValueType {

	FLOAT(GL_FLOAT);
	
	private final int code;
	
	ValueType(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
