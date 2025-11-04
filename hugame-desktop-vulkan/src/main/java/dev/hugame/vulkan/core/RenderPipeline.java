package dev.hugame.vulkan.core;

import dev.hugame.vulkan.pipeline.VulkanPipeline;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO: Make this class VulkanRenderPipeline implements RenderPipeline which would be in the core
// module
@RequiredArgsConstructor
public class RenderPipeline {
  @Getter private final VulkanPipeline pipeline;
}
