package dev.hugame.vulkan.renderer;

import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.model.Model;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.buffer.BufferUtils;
import dev.hugame.vulkan.buffer.VulkanShaderStorageBuffer;
import dev.hugame.vulkan.buffer.VulkanUniformBuffer;
import dev.hugame.vulkan.core.*;
import dev.hugame.vulkan.layout.DescriptorSource;
import dev.hugame.vulkan.model.VulkanModel;
import dev.hugame.vulkan.pipeline.RenderPipeline;
import java.util.*;
import java.util.stream.IntStream;
import lombok.Getter;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class ModelRenderer {
  private static final int VERTEX_SHADER_UNIFORM_BUFFER_SIZE = 2 * 16 * Float.BYTES;
  private static final int FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE = 6 * Float.BYTES;

  private final RenderPipeline pipeline;
  @Getter private final List<VulkanUniformBuffer> vertexShaderUniformBuffers;
  @Getter private final List<VulkanUniformBuffer> fragmentShaderUniformBuffers;

  private final VulkanShaderStorageBuffer<Material> materialBuffer;
  private final VulkanShaderStorageBuffer<PointLight> pointLightBuffer;
  private final VulkanShaderStorageBuffer<SpotLight> spotLightBuffer;
  private final VulkanShaderStorageBuffer<DirectionalLight> directionalLightBuffer;

  private final Map<Model, List<Transform>> modelInstanceData;
  @Getter private PerspectiveCamera camera;

  public ModelRenderer(VulkanGraphics graphics) {
    // TODO: Let this class instantiate the ModelPipeline
    this.pipeline = graphics.getModelPipeline();

    this.vertexShaderUniformBuffers =
        IntStream.range(0, graphics.getSwapChain().getImageViewHandles().size())
            .mapToObj(
                ignored -> VulkanUniformBuffer.create(graphics, VERTEX_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.fragmentShaderUniformBuffers =
        IntStream.range(0, graphics.getSwapChain().getImageViewHandles().size())
            .mapToObj(
                ignored ->
                    VulkanUniformBuffer.create(graphics, FRAGMENT_SHADER_UNIFORM_BUFFER_SIZE))
            .toList();

    this.materialBuffer = VulkanShaderStorageBuffer.create(graphics, Material.SIZE_IN_BYTES, 1);
    this.pointLightBuffer =
        VulkanShaderStorageBuffer.create(graphics, PointLight.SIZE_IN_BYTES, 10);
    this.spotLightBuffer = VulkanShaderStorageBuffer.create(graphics, SpotLight.SIZE_IN_BYTES, 10);
    this.directionalLightBuffer =
        VulkanShaderStorageBuffer.create(graphics, DirectionalLight.SIZE_IN_BYTES, 10);

    this.modelInstanceData = new HashMap<>();

    camera = new PerspectiveCamera(new Vector3f(100, 100, 100));
    camera.lookAt(new Vector3f(0, 0, 0));
    camera.update();
  }

  public void draw(Model model, Transform transform) {
    modelInstanceData.computeIfAbsent(model, ignored -> new ArrayList<>()).add(transform);
  }

  public void flush(VulkanGraphics graphics) {
    var renderer = graphics.getRenderer();
    var currentImageIndex = renderer.getCurrentImageIndex();

    if (modelInstanceData.isEmpty()) {
      return;
    }

    var currentFrameVertexShaderUniformBuffer = vertexShaderUniformBuffers.get(currentImageIndex);
    currentFrameVertexShaderUniformBuffer.update(
        buffer -> {
          camera.getViewMatrix().get(0, buffer);
          buffer.position(buffer.position() + 16 * Float.BYTES);
          camera.getProjectionMatrix().get(buffer);
        });

    var currentFrameFragmentShaderUniformBuffer =
        fragmentShaderUniformBuffers.get(currentImageIndex);
    currentFrameFragmentShaderUniformBuffer.update(
        buffer -> {
          camera.getPosition().get(buffer);
          buffer.position(buffer.position() + 3 * Float.BYTES);
          buffer.putInt(pointLightBuffer.getItemCount());
          buffer.putInt(spotLightBuffer.getItemCount());
          buffer.putInt(directionalLightBuffer.getItemCount());
        });

    for (var modelAndTransforms : modelInstanceData.entrySet()) {
      var uncheckedModel = modelAndTransforms.getKey();
      var instanceTransforms = modelAndTransforms.getValue();
      if (!(uncheckedModel instanceof VulkanModel model)) {
        throw new RuntimeException("Invalid type of Model: " + uncheckedModel.getClass());
      }

      renderModel(graphics, model, instanceTransforms);
    }

    modelInstanceData.clear();
  }

  public void updateEnvironment(Environment environment) {
    var pointLights = environment.getPointLights();
    var spotLights = environment.getSpotLights();
    var directionalLights = environment.getDirectionalLights();

    pointLightBuffer.refill(pointLights);
    spotLightBuffer.refill(spotLights);
    directionalLightBuffer.refill(directionalLights);
  }

  public void setMaterials(List<Material> materials) {
    materialBuffer.fill(materials);
  }

  private void renderModel(VulkanGraphics graphics, VulkanModel model, List<Transform> transforms) {
    var renderer = graphics.getRenderer();
    var currentImageIndex = renderer.getCurrentImageIndex();

    var instanceCount = transforms.size();
    var transformSizeBytes = 16 * Float.BYTES;
    var instanceDataBuffer = MemoryUtil.memAlloc(instanceCount * transformSizeBytes).rewind();

    var instanceOffset = 0;
    for (var transform : transforms) {
      var transformationMatrix = transform.getMatrix();
      transformationMatrix.get(instanceOffset * transformSizeBytes, instanceDataBuffer);

      instanceOffset++;
    }

    var instanceBuffer = model.getInstanceBuffer();
    var instanceBufferContent = instanceBuffer.getBuffer();
    BufferUtils.fillWithStagingBuffer(graphics, instanceBufferContent, instanceDataBuffer);
    MemoryUtil.memFree(instanceDataBuffer);

    var textureArrays = model.getTextureArrays();

    var descriptorSets = graphics.getModelPipeline().getDescriptorSets();
    var currentDescriptorSet = descriptorSets.get(currentImageIndex);

    var currentFrameVertexShaderUniformBuffer = vertexShaderUniformBuffers.get(currentImageIndex);
    var currentFrameFragmentShaderUniformBuffer =
        fragmentShaderUniformBuffers.get(currentImageIndex);

    currentDescriptorSet.write(
        graphics,
        DescriptorSource.fromUniformBuffer(currentFrameVertexShaderUniformBuffer),
        DescriptorSource.fromUniformBuffer(currentFrameFragmentShaderUniformBuffer),
        DescriptorSource.fromTextureArrays(textureArrays, 32),
        DescriptorSource.fromShaderStorageBuffer(materialBuffer),
        DescriptorSource.fromShaderStorageBuffer(pointLightBuffer),
        DescriptorSource.fromShaderStorageBuffer(spotLightBuffer),
        DescriptorSource.fromShaderStorageBuffer(directionalLightBuffer));

    var frameBuffer = graphics.getFrameBuffers().get(currentImageIndex);

    var renderInfo =
        RenderInfo.builder()
            .pipeline(pipeline)
            .frameBuffer(frameBuffer)
            .vertexBuffers(List.of(model.getVertexBuffer(), instanceBuffer))
            .indexBuffer(model.getIndexBuffer())
            .indexCount(model.getIndexCount())
            .descriptorSet(currentDescriptorSet)
            .commandBuffer(graphics.getCommandBuffer())
            .build();

    graphics.render(renderInfo);

    transforms.clear();
  }
}
