package dev.hugame.vulkan.buffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class VulkanPersistentlyMappedBuffer extends VulkanBuffer {
	private ByteBuffer mappedMemory;

	public VulkanPersistentlyMappedBuffer(long handle, ByteBuffer mappedMemory) {
		super(handle, MemoryUtil.memAddress(mappedMemory));

		this.mappedMemory = mappedMemory;
	}

	public ByteBuffer getMappedMemory() {
		return mappedMemory;
	}
}
