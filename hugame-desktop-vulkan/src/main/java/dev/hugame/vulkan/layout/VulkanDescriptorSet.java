package dev.hugame.vulkan.layout;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class VulkanDescriptorSet {
  private final long handle;

  public VulkanDescriptorSet(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }

  public void write(VulkanGraphics graphics, DescriptorSource... descriptorSources) {
    try (var memoryStack = stackPush()) {
      var descriptorWriteBuffer =
          VkWriteDescriptorSet.calloc(descriptorSources.length, memoryStack);

      for (int i = 0; i < descriptorSources.length; i++) {
        var descriptorSource = descriptorSources[i];

        var descriptorWrite = descriptorWriteBuffer.get(i);
        descriptorWrite
            .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
            .dstSet(handle)
            .dstBinding(i)
            .dstArrayElement(0);

        descriptorSource.write(graphics, new DescriptorWriteData(descriptorWrite), memoryStack);
      }

      vkUpdateDescriptorSets(graphics.getDevice().getLogical(), descriptorWriteBuffer, null);
    }
  }
}
