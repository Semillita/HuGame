package dev.hugame.vulkan.core;

import dev.hugame.core.Renderer;
import dev.hugame.environment.Environment;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.model.Model;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.buffer.Vertex;
import dev.hugame.vulkan.commands.*;
import dev.hugame.vulkan.model.ModelFactory;
import dev.hugame.vulkan.model.VulkanModel;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;

import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanRenderer implements Renderer {
	private static final long UNSIGNED_LONG_MAX_VALUE = 0xFFFFFFFFFFFFFFFFL;

	public static final Vertex[] VERTICES = {
			new Vertex(vec3(-0.5f, -0.5f, 0.0f), vec3(1.0f, 0.5f, 0.0f), vec2(1f, 0f)),
			new Vertex(vec3(0.5f, -0.5f, 0.0f), vec3(1.0f, 0.0f, 0.0f), vec2(0f, 0f)),
			new Vertex(vec3(0.5f, 0.5f, 0.0f), vec3(0.0f, 1.0f, 0.0f), vec2(0f, 1f)),
			new Vertex(vec3(-0.5f, 0.5f, 0.0f), vec3(0.0f, 0.0f, 1.0f), vec2(1f, 1f)),

			new Vertex(vec3(-0.5f, -0.5f, -0.5f), vec3(1.0f, 0.5f, 0.0f), vec2(1f, 0f)),
			new Vertex(vec3(0.5f, -0.5f, -0.5f), vec3(1.0f, 0.0f, 0.0f), vec2(0f, 0f)),
			new Vertex(vec3(0.5f, 0.5f, -0.5f), vec3(0.0f, 1.0f, 0.0f), vec2(0f, 1f)),
			new Vertex(vec3(-0.5f, 0.5f, -0.5f), vec3(0.0f, 0.0f, 1.0f), vec2(1f, 1f))
	};

	public static final int[] INDICES = {0, 1, 2, 2, 3, 0, 	4, 5, 6, 6, 7, 4};

	private final VulkanGraphics graphics;
	private PerspectiveCamera camera;

	private final BindPipelineCommand bindPipelineCommand;
	private final SetViewportCommand setViewportCommand;
	private final SetScissorCommand setScissorCommand;
	private final EndRenderPassCommand endRenderPassCommand;

	private int currentImageIndex;

	VulkanRenderer(VulkanGraphics graphics) {
		this.graphics = graphics;

		camera = new PerspectiveCamera(new Vector3f(200f, 200f, 200f));
		camera.lookAt(new Vector3f(0, 0, 0));
		camera.update();

		this.setViewportCommand = new SetViewportCommand();
		this.setScissorCommand = new SetScissorCommand();
		this.bindPipelineCommand = new BindPipelineCommand();
		this.endRenderPassCommand = new EndRenderPassCommand();
	}

	@Override
	public void create() {
	}

	@Override
	public void beginFrame() {
		var device = graphics.getDevice();
		var logicalDevice = device.getLogical();
		var inFlightFenceHandle = graphics.getInFlightFence().getHandle();

		var imageIndex = acquireNextImage();
		if (imageIndex == null) {
			return;
		}

		vkWaitForFences(logicalDevice, inFlightFenceHandle, true, UNSIGNED_LONG_MAX_VALUE);
		vkResetFences(logicalDevice, inFlightFenceHandle);

		this.currentImageIndex = imageIndex;
	}

	@Override
	public void draw(Model model, Transform transform, Material material) {

	}

	@Override
	public void draw(Model model, Transform transform) {
		var vulkanModel = (VulkanModel) model;

		var device = graphics.getDevice();
		var inFlightFenceHandle = graphics.getInFlightFence().getHandle();

		/*var imageIndex = acquireNextImage();
		if (imageIndex == null) {
			return;
		}

		vkWaitForFences(logicalDevice, inFlightFenceHandle, true, UNSIGNED_LONG_MAX_VALUE);
		vkResetFences(logicalDevice, inFlightFenceHandle);*/

		var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);

		var vertexBuffers = List.of(vulkanModel.getVertexBuffer());
		var indexBuffer = vulkanModel.getIndexBuffer();

		var descriptorSets = graphics.getDescriptorSets();

		var commandBuffer = graphics.getCommandBuffer();
		commandBuffer.reset();
		commandBuffer.record(
				graphics,
				new BeginRenderPassCommand(frameBuffer),
				bindPipelineCommand,
				setViewportCommand,
				setScissorCommand,
				new BindVertexBufferCommand(vertexBuffers),
				new BindIndexBufferCommand(indexBuffer),
				new BindDescriptorSetsCommand(descriptorSets.get(currentImageIndex)),
				new DrawCommand(vulkanModel.getIndexCount()),
				endRenderPassCommand);

		var imageAvailableSemaphore = graphics.getImageAvailableSemaphore();
		var renderFinishedSemaphore = graphics.getRenderFinishedSemaphore();

		var currentFrameUniformBuffer = graphics.getUniformBuffers().get(currentImageIndex);
		currentFrameUniformBuffer.update(graphics, camera, transform);

		try (var memoryStack = stackPush()) {
			var submitInfo = VkSubmitInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
					.waitSemaphoreCount(1)
					.pWaitSemaphores(memoryStack.longs(imageAvailableSemaphore.getHandle()))
					.pWaitDstStageMask(memoryStack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
					.pCommandBuffers(memoryStack.pointers(commandBuffer.getVkCommandBuffer()))
					.pSignalSemaphores(memoryStack.longs(renderFinishedSemaphore.getHandle()));

			if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, inFlightFenceHandle) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to submit queue");
			}
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void endFrame() {
		var device = graphics.getDevice();
		var inFlightFenceHandle = graphics.getInFlightFence().getHandle();

		/*var imageIndex = acquireNextImage();
		if (imageIndex == null) {
			return;
		}

		vkWaitForFences(logicalDevice, inFlightFenceHandle, true, UNSIGNED_LONG_MAX_VALUE);
		vkResetFences(logicalDevice, inFlightFenceHandle);*/

		/*var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);

		var vertexBuffers = List.of(graphics.getVertexBuffer());
		var indexBuffer = graphics.getIndexBuffer();

		var descriptorSets = graphics.getDescriptorSets();

		var commandBuffer = graphics.getCommandBuffer();
		commandBuffer.reset();
		commandBuffer.record(
				graphics,
				new BeginRenderPassCommand(frameBuffer),
				bindPipelineCommand,
				setViewportCommand,
				setScissorCommand,
				new BindVertexBufferCommand(vertexBuffers),
				new BindIndexBufferCommand(indexBuffer),
				new BindDescriptorSetsCommand(descriptorSets.get(currentImageIndex)),
				new DrawCommand(INDICES.length),
				endRenderPassCommand);



		var imageAvailableSemaphore = graphics.getImageAvailableSemaphore();
		var renderFinishedSemaphore = graphics.getRenderFinishedSemaphore();

		var currentFrameUniformBuffer = graphics.getUniformBuffers().get(currentImageIndex);
		currentFrameUniformBuffer.update(graphics);*/

		try (var memoryStack = stackPush()) {
			/*var signalSemaphoreHandlesBuffer = memoryStack.longs(renderFinishedSemaphore.getHandle());

			var submitInfo = VkSubmitInfo.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
					.waitSemaphoreCount(1)
					.pWaitSemaphores(memoryStack.longs(imageAvailableSemaphore.getHandle()))
					.pWaitDstStageMask(memoryStack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
					.pCommandBuffers(memoryStack.pointers(commandBuffer.getVkCommandBuffer()))
					.pSignalSemaphores(signalSemaphoreHandlesBuffer);

			if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, inFlightFenceHandle) != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to submit queue");
			}*/

			var presentInfo = VkPresentInfoKHR.calloc(memoryStack)
					.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
					.pWaitSemaphores(memoryStack.longs(graphics.getRenderFinishedSemaphore().getHandle()))
					.swapchainCount(1)
					.pSwapchains(memoryStack.longs(graphics.getSwapChain().getHandle()))
					.pImageIndices(memoryStack.ints(currentImageIndex));

			var presentResult = vkQueuePresentKHR(device.getPresentQueue(), presentInfo);

			if (presentResult == VK_ERROR_OUT_OF_DATE_KHR
					|| presentResult == VK_SUBOPTIMAL_KHR
					|| graphics.frameBufferResized()) {
				graphics.setFrameBufferResized(false);
				graphics.recreateSwapChain();
			} else if (presentResult != VK_SUCCESS) {
				throw new RuntimeException("[HuGame] Failed to queue present");
			}
		}
	}

	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public void updateEnvironment(Environment environment) {

	}

	private Integer acquireNextImage() {
		var imageIndexBuffer = MemoryUtil.memAllocInt(1);

		var result = vkAcquireNextImageKHR(
				graphics.getDevice().getLogical(),
				graphics.getSwapChain().getHandle(),
				UNSIGNED_LONG_MAX_VALUE,
				graphics.getImageAvailableSemaphore().getHandle(),
				VK_NULL_HANDLE,
				imageIndexBuffer);

		if (result == VK_ERROR_OUT_OF_DATE_KHR || graphics.frameBufferResized()) {
			graphics.setFrameBufferResized(false);
			graphics.recreateSwapChain();

			return null;
		} else if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
			throw new RuntimeException("[HuGame] Failed to acquire next swap chain image");
		}

		var imageIndex = imageIndexBuffer.get(0);
		MemoryUtil.memFree(imageIndexBuffer);

		return imageIndex;
	}

	private static Vector2f vec2(float x, float y) {
		return new Vector2f(x, y);
	}

	private static Vector3f vec3(float x, float y, float z) {
		return new Vector3f(x, y, z);
	}
}
