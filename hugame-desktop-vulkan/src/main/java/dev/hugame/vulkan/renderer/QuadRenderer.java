package dev.hugame.vulkan.renderer;

import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.commands.*;
import dev.hugame.vulkan.core.*;
import dev.hugame.vulkan.layout.DescriptorSource;
import dev.hugame.vulkan.pipeline.RenderPipeline;
import java.util.List;
import java.util.stream.IntStream;

public class QuadRenderer {
  private static final int QUAD_PIPELINE_UNIFORM_BUFFER_SIZE = 2 * 16 * Float.BYTES;

  private final RenderPipeline pipeline;
  private final List<VulkanUniformBuffer> uniformBuffers;

  public QuadRenderer(VulkanGraphics graphics) {
    this.pipeline = graphics.getQuadPipeline();

    this.uniformBuffers =
        IntStream.range(0, graphics.getFramesInFlightCount())
            .mapToObj(
                ignored -> VulkanUniformBuffer.create(graphics, QUAD_PIPELINE_UNIFORM_BUFFER_SIZE))
            .toList();
  }

  public void renderBatch(VulkanGraphics graphics, VulkanBatch batch) {
    var renderer = graphics.getRenderer();
    var currentImageIndex = renderer.getCurrentImageIndex();

    var frameBuffer = graphics.getFrameBuffers().get(renderer.getCurrentImageIndex());

    var currentFrameUniformBuffer = uniformBuffers.get(currentImageIndex);

    var descriptorSets = graphics.getQuadPipeline().getDescriptorSets();
    var currentDescriptorSet = descriptorSets.get(currentImageIndex);

    var commandBuffer = graphics.getCommandBuffer();

    var vertexBuffer = batch.getVertexBuffer();
    var indexBuffer = batch.getIndexBuffer();

    BufferUtils.fillWithStagingBuffer(
        graphics, vertexBuffer.getBuffer(), batch.getVertexDataBuffer());

    var textureArrays = batch.getTextureArrays();

    currentDescriptorSet.write(
        graphics,
        DescriptorSource.fromUniformBuffer(currentFrameUniformBuffer),
        DescriptorSource.fromTextureArrays(textureArrays, 32));

    var batchCamera = batch.getCamera();
    currentFrameUniformBuffer.update(
        buffer -> {
          batchCamera.getViewMatrix().get(0, buffer);
          batchCamera.getProjectionMatrix().get(16 * Float.BYTES, buffer);
        });

    var renderInfo =
        RenderInfo.builder()
            .pipeline(pipeline)
            .frameBuffer(frameBuffer)
            .vertexBuffers(List.of(vertexBuffer))
            .indexBuffer(indexBuffer)
            .indexCount(batch.getIndexCount())
            .descriptorSet(currentDescriptorSet)
            .commandBuffer(commandBuffer)
            .build();

    graphics.render(renderInfo);
  }
}
