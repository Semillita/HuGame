package io.semillita.hugame.util.buffer;

import static org.lwjgl.opengl.GL43.*;

public class DefaultBuffer extends Buffer {

	public DefaultBuffer(int handle, int target) {
		super(handle, GL_SHADER_STORAGE_BUFFER);
	}

	
	
}
