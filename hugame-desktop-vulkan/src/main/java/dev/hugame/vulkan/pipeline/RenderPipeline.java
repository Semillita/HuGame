package dev.hugame.vulkan.pipeline;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorFactory;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import java.util.List;
import lombok.Getter;

// TODO: Make this class VulkanRenderPipeline implements RenderPipeline which would be in the core
//  module
public class RenderPipeline {
  @Getter private final VulkanPipeline pipeline;
  @Getter private final List<VulkanDescriptorSet> descriptorSets;

  public RenderPipeline(
      VulkanGraphics graphics,
      DescriptorFactory descriptors,
      String vertexShaderSource,
      String fragmentShaderSource,
      boolean depthTestEnabled) {
    var descriptorSetLayout = descriptors.createDescriptorSetLayout(graphics);

    this.pipeline =
        VulkanPipeline.create(
            graphics,
            descriptors,
            descriptorSetLayout,
            vertexShaderSource,
            fragmentShaderSource,
            depthTestEnabled);

    var descriptorPool = descriptors.createDescriptorPool(graphics);

    this.descriptorSets = descriptorPool.allocateDescriptorSets(graphics, descriptorSetLayout);
  }
}
