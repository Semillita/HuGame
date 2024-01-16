package dev.hugame.vulkan.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

// TODO: Make into a class that uses a VertexLayout and creates appropriate attributes
public class VertexInput {
	private static final int POSITION_SIZE = 3;
	private static final int COLOR_SIZE = 3;
	private static final int TEXTURE_COORDINATES_SIZE = 2;

	private static final int POSITION_OFFSET = 0;
	private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
	private static final int TEXTURE_COORDINATES_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;

	public static final int VERTEX_SIZE = POSITION_SIZE + COLOR_SIZE + TEXTURE_COORDINATES_SIZE;
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

	private static final int INSTANCE_SIZE = 16;
	private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE * Float.BYTES;

	public static VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack memoryStack) {
		var buffer = VkVertexInputBindingDescription.calloc(1, memoryStack);

		// Per-vertex data
		buffer.get(0)
				.binding(0)
				.stride(VERTEX_SIZE_BYTES)
				.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

		// Per-instance data
		/*buffer.get(1)
				.binding(0)
				.stride(INSTANCE_SIZE)
				.inputRate(VK_VERTEX_INPUT_RATE_INSTANCE);*/

		return buffer;
	}

	public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack memoryStack) {
		var buffer = VkVertexInputAttributeDescription.calloc(3, memoryStack);

		buffer.get(0)
				.binding(0)
				.location(0)
				.format(VK_FORMAT_R32G32B32_SFLOAT)
				.offset(POSITION_OFFSET);

		buffer.get(1)
				.binding(0)
				.location(1)
				.format(VK_FORMAT_R32G32B32_SFLOAT)
				.offset(COLOR_OFFSET);

		buffer.get(2)
				.binding(0)
				.location(2)
				.format(VK_FORMAT_R32G32_SFLOAT)
				.offset(TEXTURE_COORDINATES_OFFSET);

		return buffer;
	}
}
