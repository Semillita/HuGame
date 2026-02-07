package dev.hugame.vulkan.text;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.text.Font;
import dev.hugame.util.Files;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.core.RenderInfo;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorSource;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import dev.hugame.vulkan.pipeline.RenderPipeline;
import java.awt.Dimension;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;
import org.joml.Vector2f;

public class TextRenderer {
  private static final int VERTEX_SHADER_UNIFORM_BUFFER_SIZE = 2 * 16 * Float.BYTES;
  private static final int FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE = Float.BYTES;

  @Getter
  private final VulkanDescriptorSetLayout descriptorSetLayout; // TODO: Make into local variable

  @Getter
  private final List<VulkanUniformBuffer>
      vertexShaderUniformBuffers; // TODO: Maybe put into per-frame struct

  @Getter private final List<VulkanUniformBuffer> fragmentShaderUniformBuffers;
  private final TextBatch textBatch;
  private final RenderPipeline renderPipeline;

  public TextRenderer(VulkanGraphics graphics) {
    var descriptors = new DefaultTextPipelineDescriptors();
    this.descriptorSetLayout = descriptors.createDescriptorSetLayout(graphics);

    this.vertexShaderUniformBuffers =
        IntStream.range(0, graphics.getFramesInFlightCount())
            .mapToObj(
                ignored -> VulkanUniformBuffer.create(graphics, VERTEX_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.fragmentShaderUniformBuffers =
        IntStream.range(0, graphics.getFramesInFlightCount())
            .mapToObj(
                ignored ->
                    VulkanUniformBuffer.create(graphics, FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.textBatch =
        new TextBatch(
            graphics,
            new Camera2D(
                () -> graphics.getSurfaceContext().getSize(),
                new Vector2f(960, 540),
                new Dimension(960, 540)));

    this.renderPipeline =
        new RenderPipeline(
            graphics,
            new DefaultTextPipelineDescriptors(),
            Files.read("/shaders/vulkan_text_vertex_shader.glsl").orElseThrow(),
            Files.read("/shaders/vulkan_text_fragment_shader.glsl").orElseThrow(),
            false);
  }

  public void drawText(
      VulkanGraphics graphics, String text, Font font, int fontSize, int x, int y) {
    if (textBatch.isFull()) {
      flushTextRenderer(graphics);
    }

    textBatch.add(text, font, fontSize, x, y);
  }

  public void flushTextRenderer(VulkanGraphics graphics) {
    Logger.pushScope("VulkanRenderer#flushTextRenderer");
    var device = graphics.getDevice();
    var swapChain = graphics.getSwapChain();
    var renderer = graphics.getRenderer();
    var currentImageIndex = renderer.getCurrentImageIndex();
    var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);
    var swapChainColorImageHandle = swapChain.getImageHandles().get(currentImageIndex);
    var depthBufferImage = swapChain.getDepthBuffer().getImage();

    var camera = textBatch.getCamera();
    // 1) Update uniform buffer content
    var currentFrameVertexShaderUniformBuffer = vertexShaderUniformBuffers.get(currentImageIndex);
    currentFrameVertexShaderUniformBuffer.update(
        buffer -> {
          camera.getViewMatrix().get(0, buffer);
          camera.getProjectionMatrix().get(16 * Float.BYTES, buffer);
        });

    var pxRange = (250f / 32f) * (float) HuGame.MSDF_RANGE;
    Logger.log("Drawing text with pxRange=" + pxRange);

    var currentFrameFragmentShaderUniformBuffer =
        fragmentShaderUniformBuffers.get(currentImageIndex);
    currentFrameFragmentShaderUniformBuffer.update(
        buffer -> {
          buffer.putFloat(pxRange); // TODO: Figure out the actual range
        });

    var currentDescriptorSet = renderPipeline.getDescriptorSets().get(currentImageIndex);
    var commandBuffer = graphics.getCommandBuffer();

    var vertexBuffer = textBatch.getVertexBuffer();
    var indexBuffer = textBatch.getIndexBuffer();
    var vertexDataBuffer = textBatch.getVertexDataBuffer();

    BufferUtils.fillWithStagingBuffer(graphics, vertexBuffer.getBuffer(), vertexDataBuffer);

    var textureArrays = textBatch.getTextureArrays();

    currentDescriptorSet.write(
        graphics,
        DescriptorSource.fromUniformBuffer(currentFrameVertexShaderUniformBuffer),
        DescriptorSource.fromUniformBuffer(currentFrameFragmentShaderUniformBuffer),
        DescriptorSource.fromTextureArrays(textureArrays, 32));

    var renderInfo =
        RenderInfo.builder()
            .pipeline(renderPipeline)
            .frameBuffer(frameBuffer)
            .vertexBuffers(List.of(vertexBuffer))
            .indexBuffer(indexBuffer)
            .indexCount(textBatch.getIndexCount())
            .descriptorSet(currentDescriptorSet)
            .commandBuffer(commandBuffer)
            .build();

    graphics.render(renderInfo);

    textBatch.clear();

    Logger.popScope();
  }
}
