package dev.hugame.vulkan.image;

public class VulkanImage {
	private final long handle;
	private final long memoryHandle;

	public VulkanImage(long handle, long memoryHandle) {
		this.handle = handle;
		this.memoryHandle = memoryHandle;
	}

	public long getHandle() {
		return handle;
	}

	public long getMemoryHandle() {
		return memoryHandle;
	}
}
