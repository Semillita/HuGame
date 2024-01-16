package dev.hugame.desktop.gl.buffer;

import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.*;

import java.util.List;

import org.lwjgl.system.MemoryUtil;

import dev.hugame.util.Bufferable;

public abstract class GLShaderStorageBuffer<T extends Bufferable> extends Buffer {

	private final int bytesPerItem;
	private int maxItems;

	public GLShaderStorageBuffer(int handle, int bytesPerItem) {
		super(handle, GL_SHADER_STORAGE_BUFFER);
		this.bytesPerItem = bytesPerItem;
	}

	public int getMaxItems() {
		return maxItems;
	}

	public void bindBase(int index) {
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, index, super.getHandle());
	}

	public void fill(List<T> items) {
		fill(items, items.size());
	}

	public void allocate(int maxItems) {
		this.maxItems = maxItems;

		final var bufferSize = maxItems * bytesPerItem;
		super.bind();
		super.bufferData(bufferSize);
	}

	public void fill(List<T> items, int maxItems) {
		this.maxItems = maxItems;

		final var bufferSize = maxItems * bytesPerItem;
		final var nativeBuffer = MemoryUtil.memAlloc(bufferSize);

		for (var item : items) {
			nativeBuffer.put(item.getBytes());
		}

		nativeBuffer.flip();
		super.bind();
		super.bufferData(nativeBuffer);

		MemoryUtil.memFree(nativeBuffer);
	}

	public void refill(List<T> items) {
		final var bufferSize = items.size() * bytesPerItem;
		final var nativeBuffer = MemoryUtil.memAlloc(bufferSize);

		for (var item : items) {
			nativeBuffer.put(item.getBytes());
		}

		nativeBuffer.flip();
		super.bind();
		super.bufferSubData(nativeBuffer);

		MemoryUtil.memFree(nativeBuffer);
	}

}
