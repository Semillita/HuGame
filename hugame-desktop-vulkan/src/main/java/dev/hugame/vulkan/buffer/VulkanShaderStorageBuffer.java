package dev.hugame.vulkan.buffer;

import dev.hugame.graphics.spec.buffer.ShaderStorageBuffer;
import dev.hugame.util.Bufferable;
import dev.hugame.vulkan.core.VulkanGraphics;

import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanShaderStorageBuffer<T extends Bufferable> implements ShaderStorageBuffer<T> {
	private static final int SHADER_STORAGE_BUFFER_USAGE_FLAGS = VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
	private static final int SHADER_STORAGE_BUFFER_PROPERTIES_FLAGS = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
			| VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

	public static <T extends Bufferable> VulkanShaderStorageBuffer<T> create(VulkanGraphics graphics, int bytesPerItem, int maxItems) {
		var bufferSize = bytesPerItem * maxItems;

		var buffer = BufferUtils.createPersistentlyMappedBuffer(
				graphics,
				bufferSize,
				SHADER_STORAGE_BUFFER_USAGE_FLAGS,
				SHADER_STORAGE_BUFFER_PROPERTIES_FLAGS);

		return new VulkanShaderStorageBuffer<>(graphics, buffer, bytesPerItem, maxItems);
	}

	private VulkanShaderStorageBuffer(VulkanGraphics graphics, VulkanPersistentlyMappedBuffer buffer, int bytesPerItem, int maxItems) {
		this.graphics = graphics;
		this.buffer = buffer;
		this.bytesPerItem = bytesPerItem;
		this.maxItems = maxItems;
	}

	private final VulkanGraphics graphics;

	private VulkanPersistentlyMappedBuffer buffer;
	private final int bytesPerItem;
	private int maxItems;

	@Override
	public int getMaxItems() {
		return maxItems;
	}

	@Override
	public void allocate(int maxItems) {
		BufferUtils.destroyBuffer(graphics, buffer);
		this.buffer = BufferUtils.createPersistentlyMappedBuffer(
				graphics,
				bytesPerItem * maxItems,
				SHADER_STORAGE_BUFFER_USAGE_FLAGS,
				SHADER_STORAGE_BUFFER_PROPERTIES_FLAGS);

		this.maxItems = maxItems;
	}

	@Override
	public void fill(List<T> items, int maxItems) {
		allocate(maxItems);

		var targetBuffer = buffer.getMappedMemory().rewind();
		for (var item : items) {
			targetBuffer.put(item.getBytes());
		}
	}

	@Override
	public void refill(List<T> items) {
		var targetBuffer = buffer.getMappedMemory().rewind();
		for (var item : items) {
			targetBuffer.put(item.getBytes());
		}
	}
}
