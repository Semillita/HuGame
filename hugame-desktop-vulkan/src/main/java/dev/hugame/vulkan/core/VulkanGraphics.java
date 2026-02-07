package dev.hugame.vulkan.core;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.graphics.*;
import dev.hugame.graphics.model.Model;
import dev.hugame.graphics.text.Font;
import dev.hugame.graphics.text.FontMetadata;
import dev.hugame.graphics.text.ResolvedFont;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.Files;
import dev.hugame.util.ImageLoader;
import dev.hugame.vulkan.commands.BeginRenderPassCommand;
import dev.hugame.vulkan.commands.BindDescriptorSetsCommand;
import dev.hugame.vulkan.commands.BindIndexBufferCommand;
import dev.hugame.vulkan.commands.BindPipelineCommand;
import dev.hugame.vulkan.commands.BindVertexBuffersCommand;
import dev.hugame.vulkan.commands.DrawCommand;
import dev.hugame.vulkan.commands.VulkanCommand;
import dev.hugame.vulkan.image.ImageUtils;
import dev.hugame.vulkan.image.VulkanImageView;
import dev.hugame.vulkan.layout.implementation.DefaultModelPipelineDescriptors;
import dev.hugame.vulkan.layout.implementation.DefaultQuadPipelineDescriptors;
import dev.hugame.vulkan.model.ModelFactory;
import dev.hugame.vulkan.pipeline.RenderPipeline;
import dev.hugame.vulkan.pipeline.shader.VulkanShader;
import dev.hugame.vulkan.surface.VulkanSurface;
import dev.hugame.vulkan.surface.VulkanSurfaceContext;
import dev.hugame.vulkan.sync.*;
import dev.hugame.vulkan.texture.TextureCollector;
import dev.hugame.vulkan.texture.VulkanTexture;
import dev.hugame.vulkan.types.ImageAspect;
import dev.hugame.vulkan.types.ImageFormat;
import dev.hugame.vulkan.types.ImageType;
import dev.hugame.vulkan.types.ImageViewType;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

// TODO: Move internal vulkan stuff into a new VulkanContext class and let
//  this class be simply an external API. This class can own the
//  VulkanContext instance. Stuff like the swapchain that will always be
//  constant no matter which features are implemented can belong to the
//  VulkanContext whilst pipelines and other such things should belong to
//  respective renderers like ModelRenderer, QuadRenderer, TextRenderer.
public class VulkanGraphics implements Graphics {
  private static final boolean VALIDATION_LAYERS_ENABLED = true;

  @Getter
  private final VulkanSurfaceContext
      surfaceContext; // TODO: Maybe use some identity interface instead

  @Getter private final VulkanInstance instance;
  private final VulkanDebugMessenger debugMessenger;
  @Getter private final VulkanSurface surface;
  @Getter private final VulkanDevice device;
  @Getter private VulkanSwapChain swapChain;

  @Getter private final VulkanCommandPool commandPool;

  @Getter
  private List<VulkanSwapChainFrameBuffer>
      frameBuffers; // TODO: Maybe make these belong to the swap chain

  @Getter private final VulkanCommandBuffer commandBuffer;
  @Getter private final List<InFlightFrame> framesInFlight;
  private final VulkanRenderer renderer;

  @Getter private final RenderPipeline modelPipeline;
  @Getter private final RenderPipeline quadPipeline;

  private final ModelFactory modelFactory;
  private final TextureCollector textureCollector;
  @Getter private final VulkanTexture defaultTexture;

  @Setter private boolean frameBufferResized = false;

  @Getter private Vector4f clearColor;

  private int frame = 0;

  public VulkanGraphics(VulkanSurfaceContext surfaceContext) {
    if (VALIDATION_LAYERS_ENABLED) {
      VulkanValidations.assertValidationLayersSupported();
    }

    this.surfaceContext = surfaceContext;

    this.instance = VulkanInstance.create(surfaceContext, VALIDATION_LAYERS_ENABLED);
    this.debugMessenger = VALIDATION_LAYERS_ENABLED ? VulkanDebugMessenger.create(instance) : null;
    this.surface = surfaceContext.createSurface(instance);
    this.device = VulkanDevice.create(this);
    this.swapChain = VulkanSwapChain.create(this, surfaceContext);

    this.modelPipeline =
        new RenderPipeline(
            this,
            new DefaultModelPipelineDescriptors(),
            Files.read("/shaders/vulkan_model_vertex_shader.glsl").orElseThrow(),
            Files.read("/shaders/vulkan_model_fragment_shader.glsl").orElseThrow(),
            true);
    this.quadPipeline =
        new RenderPipeline(
            this,
            new DefaultQuadPipelineDescriptors(),
            Files.read("/shaders/vulkan_quad_vertex_shader.glsl").orElseThrow(),
            Files.read("/shaders/vulkan_quad_fragment_shader.glsl").orElseThrow(),
            false);

    this.frameBuffers = VulkanSwapChainFrameBuffer.createAll(this);
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

    var defaultTextureBytes = Files.readBytes("/default_texture.png").orElseThrow();
    this.modelFactory = new ModelFactory();
    this.textureCollector = new TextureCollector();
    this.defaultTexture = createTexture(ImageLoader.read(defaultTextureBytes, 4));

    // Don't have the graphics instance communicate with the window. Instead, have the engine call
    // VulkanGraphics#frameBufferResizeCallback, and listen to window updates.
    this.surfaceContext.addResizeListener(this::frameBufferResizeCallback);

    clearColor = new Vector4f(0, 0, 0, 1);
  }

  @Override
  public VulkanTexture createTexture(ResolvedTexture resolvedTexture) {
    return textureCollector.addTexture(resolvedTexture);
  }

  @Override
  public Font createFont(ResolvedFont resolvedFont) {
    var atlasTexture = createTexture(resolvedFont.getAtlas());

    var metadata = resolvedFont.getMetadata();
    return new Font() {
      @Override
      public Texture getTexture() {
        return atlasTexture;
      }

      @Override
      public FontMetadata getMetadata() {
        return metadata;
      }
    };
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
  public void endFrame() {
    frame++;
  }

  @Override
  public void swapBuffers() {}

  @Override
  public void clear(float red, float green, float blue, float alpha) {}

  @Override
  public void setClearColor(float red, float green, float blue, float alpha) {
    clearColor = new Vector4f(red, green, blue, alpha);
  }

  @Override
  public FrameBuffer getSwapChainFrameBuffer() {
    var inFlightFrameIndex = frame % getFramesInFlightCount();

    return frameBuffers.get(inFlightFrameIndex);
  }

  @Override
  public FrameBuffer createFrameBuffer(int width, int height) {
    // TODO: Implement

    var image =
        ImageUtils.createImage(
            this,
            width,
            height,
            ImageFormat.R8_G8_B8_A8_SRGB,
            VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
            ImageType._2D);

    var imageView =
        VulkanImageView.create(
            this, image, ImageViewType._2D, ImageFormat.R8_G8_B8_A8_SRGB, ImageAspect.COLOR);

    return null;
  }

  @Override
  public Texture createTextureFromFrameBuffer(FrameBuffer frameBuffer) {
    return null;
  }

  public void destroy() {
    instance.destroy();
  }

  public boolean validationLayersEnabled() {
    return VALIDATION_LAYERS_ENABLED;
  }

  public int getFramesInFlightCount() {
    return swapChain.getImageViewHandles().size();
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
    surfaceContext.waitUntilNotMinimized();

    var logicalDevice = device.getLogical();

    vkDeviceWaitIdle(logicalDevice);

    cleanupSwapChain();

    swapChain = VulkanSwapChain.create(this, surfaceContext);
    frameBuffers = VulkanSwapChainFrameBuffer.createAll(this);
  }

  public void render(RenderInfo renderInfo) {
    var renderPipeline = renderInfo.getPipeline();
    var pipeline = renderPipeline.getPipeline();

    var hasDrawnDuringCurrentFrame = renderer.isHasDrawnDuringCurrentFrame();

    var clearCommands =
        hasDrawnDuringCurrentFrame
            ? Collections.<VulkanCommand>emptyList()
            : renderer.getClearBufferCommands();

    var renderCommands =
        List.of(
            new BeginRenderPassCommand(pipeline.getRenderPass(), renderInfo.getFrameBuffer()),
            new BindPipelineCommand(pipeline),
            renderer.getSetViewportCommand(),
            renderer.getSetScissorCommand(),
            new BindVertexBuffersCommand(renderInfo.getVertexBuffers()),
            new BindIndexBufferCommand(renderInfo.getIndexBuffer()),
            new BindDescriptorSetsCommand(renderInfo.getDescriptorSet(), pipeline),
            new DrawCommand(renderInfo.getIndexCount(), 1),
            renderer.getEndRenderPassCommand());

    var commands = Stream.concat(clearCommands.stream(), renderCommands.stream()).toList();
    var commandBuffer = renderInfo.getCommandBuffer();
    commandBuffer.reset();
    commandBuffer.record(this, commands);

    var waitSyncPoint = hasDrawnDuringCurrentFrame ? null : getImageAvailableSemaphore();

    var submitInfo =
        new QueueSubmitInfo()
            .setCommandBuffer(commandBuffer)
            .setWaitSyncPoint(waitSyncPoint)
            .setWaitDestinationStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

    var submitResult = device.getGraphicsQueue().submit(submitInfo, null);
    if (submitResult != VulkanResult.SUCCESS) {
      throw new RuntimeException("[HuGame] Failed to submit queue");
    }

    // TODO: Set renderer has drawn current frame
    renderer.setHasDrawnDuringCurrentFrame(true);
  }

  public BinarySemaphore getImageAvailableSemaphore() {
    var inFlightFrameIndex = renderer.getFrame() % getFramesInFlightCount();
    var inFlightFrame = framesInFlight.get(inFlightFrameIndex);
    return inFlightFrame.getImageAvailableSemaphore();
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
