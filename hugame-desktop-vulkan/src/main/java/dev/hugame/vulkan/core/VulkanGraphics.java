package dev.hugame.vulkan.core;

import dev.hugame.core.Graphics;
import dev.hugame.core.GraphicsAPI;
import dev.hugame.core.Renderer;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.model.Model;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.Files;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.layout.FrameDescriptorSets;
import dev.hugame.vulkan.layout.VulkanDescriptorPool;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import dev.hugame.vulkan.model.ModelFactory;
import dev.hugame.vulkan.pipeline.VulkanPipeline;
import dev.hugame.vulkan.primitive.VulkanFence;
import dev.hugame.vulkan.primitive.VulkanSemaphore;
import dev.hugame.vulkan.texture.VulkanTexture;
import dev.hugame.window.DesktopWindow;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.util.List;
import java.util.stream.IntStream;

// TODO: Move initialisation functionality from this class to new VulkanContext
public class VulkanGraphics implements Graphics {
	private static final boolean VALIDATION_LAYERS_ENABLED = true;

	private final DesktopWindow window; // TODO: Maybe use some identity interface instead

	private final VulkanInstance instance;

	private final VulkanDebugMessenger debugMessenger;

	private final VulkanSurface surface;

	private final VulkanDevice device;

	private VulkanSwapChain swapChain;

	private final VulkanDescriptorSetLayout descriptorSetLayout;

	// TODO: Will need at least one more pipeline for batch shaders
	private final VulkanPipeline pipeline;

	private final VulkanCommandPool commandPool;

	private List<VulkanFrameBuffer> frameBuffers; // TODO: Maybe make these belong to the swap chain

	private final VulkanCommandBuffer commandBuffer;

	private final VulkanSemaphore imageAvailableSemaphore;

	private final VulkanSemaphore renderFinishedSemaphore;

	private final VulkanFence inFlightFence;

	private final Renderer renderer;

	private final List<VulkanUniformBuffer> uniformBuffers;

	private final VulkanDescriptorPool descriptorPool;

	private final List<VulkanDescriptorSet> descriptorSets;

	private final VulkanTexture texture;

	private final ModelFactory modelFactory;

	private boolean frameBufferResized = false;

	public VulkanGraphics(DesktopWindow window) {
		if (VALIDATION_LAYERS_ENABLED) {
			VulkanValidations.assertValidationLayersSupported();
		}

		this.window = window;

		this.instance = VulkanInstance.create(VALIDATION_LAYERS_ENABLED);

		this.debugMessenger = VALIDATION_LAYERS_ENABLED ? VulkanDebugMessenger.create(instance) : null;

		this.surface = VulkanSurface.create(this, window.getHandle());

		this.device = VulkanDevice.create(this);

		this.swapChain = VulkanSwapChain.create(this, window.getHandle());

		this.descriptorSetLayout = VulkanDescriptorSetLayout.create(this);

		this.pipeline = VulkanPipeline.create(this);

		this.frameBuffers = VulkanFrameBuffer.createAll(this);

		// TODO: Check if command pool needs to be created this early
		this.commandPool = VulkanCommandPool.create(this);

		this.commandBuffer = VulkanCommandBuffer.create(this);

		this.imageAvailableSemaphore = VulkanSemaphore.create(this);

		this.renderFinishedSemaphore = VulkanSemaphore.create(this);

		this.inFlightFence = VulkanFence.create(this);

		this.renderer = new VulkanRenderer(this);

		this.uniformBuffers = IntStream.range(0, swapChain.getImageViewHandles().size())
						.mapToObj(ignored -> VulkanUniformBuffer.create(this))
								.toList();

		this.descriptorPool = VulkanDescriptorPool.create(this);

		//this.texture = createTexture(Files.readBytes("/landscape.png").orElseThrow());
		this.texture = createTexture(Files.readBytes("/viking_room.png").orElseThrow());

		this.descriptorSets = descriptorPool.allocateDescriptorSets(this, texture);

		this.modelFactory = new ModelFactory();

		window.addResizeListener(this::frameBufferResizeCallback);
	}

	@Override
	public VulkanTexture createTexture(byte[] bytes) {
		return VulkanTexture.create(this, bytes);
	}

	@Override
	public GraphicsAPI getAPI() {
		return null;
	}

	@Override
	public Renderer getRenderer() {
		return renderer;
	}

	@Override
	public Model createModel(ResolvedModel resolvedModel) {
		return modelFactory.create(this, resolvedModel);
	}

	@Override
	public Batch createBatch() {
		return null;
	}

	@Override
	public void create() {

	}

	@Override
	public void swapBuffers() {
	}

	@Override
	public void clear(float red, float green, float blue) {

	}

	public void destroy() {
		instance.destroy();
	}

	public boolean validationLayersEnabled() {
		return VALIDATION_LAYERS_ENABLED;
	}

	public VulkanInstance getInstance() {
		return instance;
	}

	public VulkanSurface getSurface() {
		return surface;
	}

	public VulkanDevice getDevice() {
		return device;
	}

	public VulkanSwapChain getSwapChain() {
		return swapChain;
	}

	public VulkanDescriptorSetLayout getDescriptorSetLayout() {
		return descriptorSetLayout;
	};

	public VulkanPipeline getPipeline() {
		return pipeline;
	}

	public List<VulkanFrameBuffer> getFrameBuffers() {
		return frameBuffers;
	}

	public VulkanCommandPool getCommandPool() {
		return commandPool;
	}

	public VulkanCommandBuffer getCommandBuffer() {
		return commandBuffer;
	}

	public VulkanSemaphore getImageAvailableSemaphore() {
		return imageAvailableSemaphore;
	}

	public VulkanSemaphore getRenderFinishedSemaphore() {
		return renderFinishedSemaphore;
	}

	public VulkanFence getInFlightFence() {
		return inFlightFence;
	}

	/*public VulkanVertexBuffer getVertexBuffer() {
		return vertexBuffer;
	}

	public VulkanIndexBuffer getIndexBuffer() {
		return indexBuffer;
	}*/

	public List<VulkanUniformBuffer> getUniformBuffers() {
		return uniformBuffers;
	}

	public VulkanDescriptorPool getDescriptorPool() {
		return descriptorPool;
	}

	public List<VulkanDescriptorSet> getDescriptorSets() {
		return descriptorSets;
	}

	public int getFramesInFlightCount() {
		return swapChain.getImageViewHandles().size();
	}

	public void setFrameBufferResized(boolean resized) {
		this.frameBufferResized = resized;
	}

	public boolean frameBufferResized() {
		return frameBufferResized;
	}

	public void recreateSwapChain() {
		window.waitUntilNotMinimized();

		var logicalDevice = device.getLogical();

		vkDeviceWaitIdle(logicalDevice);

		cleanupSwapChain();

		swapChain = VulkanSwapChain.create(this, window.getHandle());
		frameBuffers = VulkanFrameBuffer.createAll(this);
	}

	private void cleanupSwapChain() {
		var logicalDevice = device.getLogical();

		for (var frameBuffer : frameBuffers) {
			vkDestroyFramebuffer(logicalDevice, frameBuffer.getHandle(), null);
		}

		for (var imageViewHandle : swapChain.getImageViewHandles()) {
			vkDestroyImageView(logicalDevice, imageViewHandle, null);
		}

		vkDestroySwapchainKHR(logicalDevice, swapChain.getHandle(), null);
	}

	private void frameBufferResizeCallback(int width, int height) {
		this.frameBufferResized = true;
	}
}
