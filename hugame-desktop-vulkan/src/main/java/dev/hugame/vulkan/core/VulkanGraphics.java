package dev.hugame.vulkan.core;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.core.Graphics;
import dev.hugame.core.GraphicsAPI;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.ResolvedTexture;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.model.Model;
import dev.hugame.graphics.spec.buffer.ShaderStorageBuffer;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.Bufferable;
import dev.hugame.util.Files;
import dev.hugame.util.ImageLoader;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.layout.VulkanDescriptorPool;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import dev.hugame.vulkan.layout.implementation.DefaultModelPipelineDescriptors;
import dev.hugame.vulkan.layout.implementation.DefaultQuadPipelineDescriptors;
import dev.hugame.vulkan.model.ModelFactory;
import dev.hugame.vulkan.pipeline.VulkanPipeline;
import dev.hugame.vulkan.pipeline.shader.VulkanShader;
import dev.hugame.vulkan.sync.*;
import dev.hugame.vulkan.texture.TextureCollector;
import dev.hugame.vulkan.texture.VulkanTexture;
import dev.hugame.window.DesktopWindow;
import java.util.List;
import java.util.stream.IntStream;
import org.joml.Vector4f;

// TODO: Move internal vulkan stuff into a new VulkanContext class and let
//  this class be simply an external API. This class can own the
//  VulkanContext instance. Stuff like the swapchain that will always be
//  constant no matter which features are implemented can belong to the
//  VulkanContext whilst pipelines and other such things should belong to
//  respective renderers like ModelRenderer, QuadRenderer, TextRenderer.
public class VulkanGraphics implements Graphics {
  private static final boolean VALIDATION_LAYERS_ENABLED = true;
  private static final int MODEL_PIPELINE_VERTEX_SHADER_UNIFORM_BUFFER_SIZE = 2 * 16 * Float.BYTES;
  private static final int MODEL_PIPELINE_FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE = 6 * Float.BYTES;
  private static final int QUAD_PIPELINE_UNIFORM_BUFFER_SIZE = 2 * 16 * Float.BYTES;

  private final DesktopWindow window; // TODO: Maybe use some identity interface instead

  private final VulkanInstance instance;
  private final VulkanDebugMessenger debugMessenger;
  private final VulkanSurface surface;
  private final VulkanDevice device;
  private VulkanSwapChain swapChain;
  private final VulkanDescriptorSetLayout modelPipelineDescriptorSetLayout;
  private final VulkanDescriptorSetLayout quadPipelineDescriptorSetLayout;
  private final VulkanPipeline modelPipeline;
  private final VulkanPipeline quadPipeline;
  private final VulkanCommandPool commandPool;
  private List<VulkanFrameBuffer> frameBuffers; // TODO: Maybe make these belong to the swap chain
  private final VulkanCommandBuffer commandBuffer;
  private final List<InFlightFrame> framesInFlight;
  private final VulkanRenderer renderer;
  private final List<VulkanUniformBuffer> modelPipelineVertexShaderUniformBuffers;
  private final List<VulkanUniformBuffer> modelPipelineFragmentShaderUniformBuffers;

  // TODO: Move these to some SwapChainFrame structure
  private final List<VulkanUniformBuffer> quadPipelineUniformBuffers;

  // TODO: Try to put along other pipeline-specific stuff
  private final VulkanDescriptorPool modelPipelineDescriptorPool;
  private final VulkanDescriptorPool quadPipelineDescriptorPool;
  private final List<VulkanDescriptorSet> modelPipelineDescriptorSets;
  private final List<VulkanDescriptorSet> quadPipelineDescriptorSets;

  private final ModelFactory modelFactory;
  private final TextureCollector textureCollector;
  private final VulkanTexture defaultTexture;

  private boolean frameBufferResized = false;

  private Vector4f clearColor;

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

    var modelPipelineDescriptors = new DefaultModelPipelineDescriptors();
    var modelPipelineVertexShaderSource =
        Files.read("/shaders/vulkan_model_vertex_shader.glsl").orElseThrow();
    var modelPipelineFragmentShaderSource =
        Files.read("/shaders/vulkan_model_fragment_shader.glsl").orElseThrow();

    var quadPipelineDescriptors = new DefaultQuadPipelineDescriptors();
    var quadPipelineVertexSource =
        Files.read("/shaders/vulkan_quad_vertex_shader.glsl").orElseThrow();
    var quadPipelineFragmentSource =
        Files.read("/shaders/vulkan_quad_fragment_shader.glsl").orElseThrow();

    this.modelPipelineDescriptorSetLayout =
        modelPipelineDescriptors.createDescriptorSetLayout(this);
    this.quadPipelineDescriptorSetLayout = quadPipelineDescriptors.createDescriptorSetLayout(this);

    this.modelPipeline =
        VulkanPipeline.create(
            this,
            modelPipelineDescriptors,
            modelPipelineDescriptorSetLayout,
            modelPipelineVertexShaderSource,
            modelPipelineFragmentShaderSource);

    this.quadPipeline =
        VulkanPipeline.create(
            this,
            quadPipelineDescriptors,
            quadPipelineDescriptorSetLayout,
            quadPipelineVertexSource,
            quadPipelineFragmentSource);

    this.frameBuffers = VulkanFrameBuffer.createAll(this);
    // TODO: Check if command pool needs to be created this early
    this.commandPool = VulkanCommandPool.create(this);
    this.commandBuffer = VulkanCommandBuffer.create(this);

    this.framesInFlight =
        IntStream.range(0, getFramesInFlightCount())
            .mapToObj(
                ignored ->
                    new InFlightFrame(
                        SyncUtils.createBinarySemaphore(this),
                        SyncUtils.createBinarySemaphore(this),
                        VulkanFence.create(this)))
            .toList();

    this.renderer = new VulkanRenderer(this);

    this.modelPipelineVertexShaderUniformBuffers =
        IntStream.range(0, swapChain.getImageViewHandles().size())
            .mapToObj(
                ignored ->
                    VulkanUniformBuffer.create(
                        this, MODEL_PIPELINE_VERTEX_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.modelPipelineFragmentShaderUniformBuffers =
        IntStream.range(0, swapChain.getImageViewHandles().size())
            .mapToObj(
                ignored ->
                    VulkanUniformBuffer.create(
                        this, MODEL_PIPELINE_FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.quadPipelineUniformBuffers =
        IntStream.range(0, getFramesInFlightCount())
            .mapToObj(
                ignored -> VulkanUniformBuffer.create(this, QUAD_PIPELINE_UNIFORM_BUFFER_SIZE))
            .toList();

    this.modelPipelineDescriptorPool = modelPipelineDescriptors.createDescriptorPool(this);
    this.quadPipelineDescriptorPool = quadPipelineDescriptors.createDescriptorPool(this);

    this.modelPipelineDescriptorSets =
        modelPipelineDescriptorPool.allocateDescriptorSets(this, modelPipelineDescriptorSetLayout);

    this.quadPipelineDescriptorSets =
        quadPipelineDescriptorPool.allocateDescriptorSets(this, quadPipelineDescriptorSetLayout);

    var defaultTextureBytes = Files.readBytes("/default_texture.png").orElseThrow();
    this.modelFactory = new ModelFactory();
    this.textureCollector = new TextureCollector();
    this.defaultTexture = createTexture(ImageLoader.read(defaultTextureBytes, 4));

    // Don't have the graphics instance communicate with the window. Instead, have the engine call
    // VulkanGraphics#frameBufferResizeCallback, and listen to window updates.
    window.addResizeListener(this::frameBufferResizeCallback);

    clearColor = new Vector4f(0, 0, 0, 1);
  }

  @Override
  public VulkanTexture createTexture(ResolvedTexture resolvedTexture) {
    return textureCollector.addTexture(resolvedTexture);
  }

  @Override
  public GraphicsAPI getAPI() {
    return GraphicsAPI.VULKAN;
  }

  @Override
  public VulkanRenderer getRenderer() {
    return renderer;
  }

  @Override
  public Model createModel(ResolvedModel resolvedModel) {
    return modelFactory.create(this, resolvedModel);
  }

  @Override
  public Batch createBatch() {
    return new VulkanBatch(this);
  }

  @Override
  public void create() {
    textureCollector.generate(this);

    // transitionSwapChainImageLayouts();
  }

  @Override
  public void swapBuffers() {}

  @Override
  public void clear(float red, float green, float blue, float alpha) {}

  @Override
  public void setClearColor(float red, float green, float blue, float alpha) {
    clearColor = new Vector4f(red, green, blue, alpha);
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

  // TODO: Probably unused?
  public VulkanDescriptorSetLayout getModelPipelineDescriptorSetLayout() {
    return modelPipelineDescriptorSetLayout;
  }
  ;

  // TODO: Probably unused?
  public VulkanDescriptorSetLayout getQuadPipelineDescriptorSetLayout() {
    return quadPipelineDescriptorSetLayout;
  }

  public VulkanPipeline getModelPipeline() {
    return modelPipeline;
  }

  public VulkanPipeline getQuadPipeline() {
    return quadPipeline;
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

  public List<InFlightFrame> getFramesInFlight() {
    return framesInFlight;
  }

  public List<VulkanUniformBuffer> getModelPipelineVertexShaderUniformBuffers() {
    return modelPipelineVertexShaderUniformBuffers;
  }

  public List<VulkanUniformBuffer> getModelPipelineFragmentShaderUniformBuffers() {
    return modelPipelineFragmentShaderUniformBuffers;
  }

  public List<VulkanUniformBuffer> getQuadPipelineUniformBuffers() {
    return quadPipelineUniformBuffers;
  }

  public List<VulkanDescriptorSet> getModelPipelineDescriptorSets() {
    return modelPipelineDescriptorSets;
  }

  public List<VulkanDescriptorSet> getQuadPipelineDescriptorSets() {
    return quadPipelineDescriptorSets;
  }

  public int getFramesInFlightCount() {
    return swapChain.getImageViewHandles().size();
  }

  public VulkanTexture getDefaultTexture() {
    return defaultTexture;
  }

  public Vector4f getClearColor() {
    return clearColor;
  }

  public void setFrameBufferResized(boolean resized) {
    this.frameBufferResized = resized;
  }

  public boolean frameBufferResized() {
    return frameBufferResized;
  }

  public VulkanTexture cast(Texture texture) {
    if (texture instanceof VulkanTexture vulkanTexture) {
      return vulkanTexture;
    }

    throw new RuntimeException(
        "[HuGame] Failed to cast texture type " + texture.getClass().getSimpleName());
  }

  public VulkanShader cast(Shader shader) {
    if (shader instanceof VulkanShader vulkanShader) {
      return vulkanShader;
    }

    throw new RuntimeException(
        "[HuGame] Failed to cast shader type " + shader.getClass().getSimpleName());
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
