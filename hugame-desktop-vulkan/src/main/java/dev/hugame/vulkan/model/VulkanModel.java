package dev.hugame.vulkan.model;

import dev.hugame.graphics.model.Model;
import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;

public class VulkanModel implements Model {
	private final VulkanVertexBuffer vertexBuffer;
	private final VulkanVertexBuffer instanceBuffer;
	private final VulkanIndexBuffer indexBuffer;

	public VulkanModel(VulkanVertexBuffer vertexBuffer, VulkanVertexBuffer instanceBuffer, VulkanIndexBuffer indexBuffer) {
		this.vertexBuffer = vertexBuffer;
		this.instanceBuffer = instanceBuffer;
		this.indexBuffer= indexBuffer;
	}

	@Override
	public int getVertexCount() {
		return 0;
	}

	@Override
	public int getIndexCount() {
		return indexBuffer.getIndexCount();
	}

	public VulkanVertexBuffer getVertexBuffer() {
		return vertexBuffer;
	}

	public VulkanVertexBuffer getInstanceBuffer() {
		return instanceBuffer;
	}

	public VulkanIndexBuffer getIndexBuffer() {
		return indexBuffer;
	}
}
