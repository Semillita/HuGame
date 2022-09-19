package dev.hugame.util.buffer;

import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.*;

import java.util.List;

import org.lwjgl.system.MemoryUtil;

import dev.hugame.util.Bufferable;
import dev.hugame.util.ByteSerializer;

public abstract class ShaderStorageBuffer<T extends Bufferable> extends Buffer {

	private final int bytesPerItem;
	private int maxItems;
	
	public ShaderStorageBuffer(int handle, int bytesPerItem, int maxItems) {
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
	
	public void fill(List<T> items, int maxItems) {
		this.maxItems = maxItems;
		final var bufferSize = Integer.BYTES + maxItems * bytesPerItem;
		final var nativeBuffer = MemoryUtil.memAlloc(bufferSize);
		for (var item : items) {
			for (var b : item.getBytes()) {
				nativeBuffer.put(b);
			}
		}
		nativeBuffer.flip();
		super.bind();
		super.bufferData(nativeBuffer);
	}
	
	public void refill(List<T> items) {
		final var bufferSize = Integer.BYTES + items.size() * bytesPerItem;
		final var nativeBuffer = MemoryUtil.memAlloc(bufferSize);
		nativeBuffer.putInt(items.size());
		for (var item : items) {
			for (var b : item.getBytes()) {
				nativeBuffer.put(b);
			}
		}
		nativeBuffer.flip();
		super.bind();
		super.bufferSubData(nativeBuffer);
	}

}
