package dev.hugame.vulkan.buffer;

import dev.hugame.graphics.Camera;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanUniformBuffer {
	private static final int UNIFORM_BUFFER_SIZE = 3 * 16 * Float.BYTES;
	private static final int UNIFORM_BUFFER_USAGE_FLAGS = VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
	private static final int UNIFORM_BUFFER_PROPERTIES_FLAGS = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
			| VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

	public static VulkanUniformBuffer create(VulkanGraphics graphics) {
		var buffer = BufferUtils.createPersistentlyMappedBuffer(
				graphics,
				UNIFORM_BUFFER_SIZE,
				UNIFORM_BUFFER_USAGE_FLAGS,
				UNIFORM_BUFFER_PROPERTIES_FLAGS);

		return new VulkanUniformBuffer(buffer);
	}

	private final VulkanPersistentlyMappedBuffer buffer;

	private VulkanUniformBuffer(VulkanPersistentlyMappedBuffer buffer) {
		this.buffer = buffer;
	}

	public VulkanPersistentlyMappedBuffer getBuffer() {
		return buffer;
	}

	public void update(VulkanGraphics graphics, Camera camera, Transform modelTransform) {
		var swapChainExtent = graphics.getSwapChain().getExtent();
		var aspectRatio = ((float) swapChainExtent.width()) / swapChainExtent.height();

		var nanoTime = System.nanoTime();

		var units = ((nanoTime / 4) % 1_000_000_000) / 1_000_000_000f;
		var degrees = units * 360;

		var uboBuffer = MemoryUtil.memCalloc(3 * 16 * Float.BYTES);

		//var model = Maths.createTransformationMatrix(new Vector3f(0, 0, 0), new Vector3f(0, 0, degrees),
		//		new Vector3f(1, 1, 1));
		var model = modelTransform
				.getMatrix();

		model.get(0, uboBuffer);

		//var view = new Matrix4f().identity()
		//		.lookAt(new Vector3f(0, 2, 2), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
		var view = camera.getViewMatrix();

		view.get(16 * Float.BYTES, uboBuffer);

		//var projection = new Matrix4f().identity()
		//		.perspective((float) Math.toRadians(45), aspectRatio, 0.1f, 10_000);
		var projection = camera.getProjectionMatrix();

		projection.get(2 * 16 * Float.BYTES, uboBuffer);


		buffer.getMappedMemory().rewind().put(uboBuffer);
	}
}
