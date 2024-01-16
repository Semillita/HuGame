package dev.hugame.vulkan.layout;

public class VulkanDescriptorSet {
	private final long handle;

	public VulkanDescriptorSet(long handle) {
		this.handle = handle;
	}

	public long getHandle() {
		return handle;
	}
}
