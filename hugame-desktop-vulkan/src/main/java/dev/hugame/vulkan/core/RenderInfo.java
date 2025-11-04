package dev.hugame.vulkan.core;

import dev.hugame.vulkan.buffer.VulkanIndexBuffer;
import dev.hugame.vulkan.buffer.VulkanVertexBuffer;
import dev.hugame.vulkan.layout.VulkanDescriptorSet;
import dev.hugame.vulkan.sync.Semaphore;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
public class RenderInfo {
  @Getter private final RenderPipeline pipeline;
  @Getter private final VulkanSwapChainFrameBuffer frameBuffer;
  @Getter private final List<VulkanVertexBuffer> vertexBuffers;
  @Getter private final VulkanIndexBuffer indexBuffer;
  @Getter private final int indexCount;
  @Getter private final VulkanDescriptorSet descriptorSet;
  @Getter private final VulkanCommandBuffer commandBuffer;
  @Getter private final Semaphore.SyncPoint waitSyncPoint;
}
