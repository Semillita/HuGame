package dev.hugame.vulkan.layout;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import java.util.List;
import java.util.stream.IntStream;
import org.lwjgl.vulkan.*;

public class VulkanDescriptorPool {
  private final long handle;

  public VulkanDescriptorPool(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }

  public List<VulkanDescriptorSet> allocateDescriptorSets(
      VulkanGraphics graphics, VulkanDescriptorSetLayout descriptorSetLayout) {
    var framesInFlightCount = graphics.getFramesInFlightCount();
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var descriptorSetLayoutHandles =
          memoryStack.longs(
              IntStream.range(0, framesInFlightCount)
                  .mapToLong(ignored -> descriptorSetLayout.getHandle())
                  .toArray());

      var descriptorSetAllocateInfo =
          VkDescriptorSetAllocateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
              .descriptorPool(handle)
              .pSetLayouts(descriptorSetLayoutHandles);

      var descriptorSetHandleBuffer = memoryStack.callocLong(framesInFlightCount);

      if (vkAllocateDescriptorSets(
              logicalDevice, descriptorSetAllocateInfo, descriptorSetHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to allocate descriptor sets");
      }

      return IntStream.range(0, framesInFlightCount)
          .mapToObj(descriptorSetHandleBuffer::get)
          .map(VulkanDescriptorSet::new)
          .toList();
    }
  }
}
