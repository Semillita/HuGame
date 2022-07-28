package io.semillita.hugame.util.buffer;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteBuffer;

public class Buffer {

	public static int newBuffer() {
		return glGenBuffers();
	}
	
	private final int handle;
	private final int target;

	public Buffer(int handle, int target) {
		this.handle = handle;
		this.target = target;
	}

	public int getHandle() {
		return handle;
	}
	
	public void bind() {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, handle);
	}
	
	public void bufferData(ByteBuffer data) {
		glBufferData(target, data, GL_DYNAMIC_DRAW);
	}
}
