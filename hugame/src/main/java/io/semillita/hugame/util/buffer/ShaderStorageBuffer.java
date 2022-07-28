package io.semillita.hugame.util.buffer;

import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.*;

import java.util.List;

import org.lwjgl.system.MemoryStack;

import io.semillita.hugame.util.Bufferable;

public abstract class ShaderStorageBuffer<T extends Bufferable> extends Buffer {

	private final int bytesPerItem;
	
	public ShaderStorageBuffer(int handle, int bytesPerItem) {
		super(handle, GL_SHADER_STORAGE_BUFFER);
		this.bytesPerItem = bytesPerItem;
	}
	
	public void bindBase() {
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, super.getHandle());
	}
	
	public void fill(List<T> items) {
		final var bufferSize = items.size() * bytesPerItem;
		final var nativeBuffer = MemoryStack.stackPush().malloc(bufferSize);
		for (var item : items) {
			for (var b : item.getBytes()) {
				nativeBuffer.put(b);
			}
		}
		nativeBuffer.flip();
		super.bind();
		super.bufferData(nativeBuffer);
	}

}
