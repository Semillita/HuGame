package dev.hugame.vulkan.image;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class VulkanImageSampler {
  public static VulkanImageSampler create(VulkanGraphics graphics) {
    var device = graphics.getDevice();

    try (var memoryStack = stackPush()) {
      var physicalDeviceProperties = VkPhysicalDeviceProperties.calloc(memoryStack);
      vkGetPhysicalDeviceProperties(device.getPhysical(), physicalDeviceProperties);

      var samplerCreateInfo =
          VkSamplerCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
              .magFilter(VK_FILTER_LINEAR)
              .minFilter(VK_FILTER_LINEAR)
              .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
              .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
              .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
              .anisotropyEnable(true)
              .maxAnisotropy(physicalDeviceProperties.limits().maxSamplerAnisotropy())
              .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
              .unnormalizedCoordinates(false)
              .compareEnable(false)
              .compareEnable(true)
              .compareOp(VK_COMPARE_OP_ALWAYS)
              .mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
              .mipLodBias(0)
              .minLod(0)
              .maxLod(0);

      var samplerHandleBuffer = memoryStack.callocLong(1);

      if (vkCreateSampler(device.getLogical(), samplerCreateInfo, null, samplerHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create sampler");
      }

      var samplerHandle = samplerHandleBuffer.get(0);

      return new VulkanImageSampler(samplerHandle);
    }
  }

  private final long handle;

  private VulkanImageSampler(long handle) {
    this.handle = handle;
  }

  public long getHandle() {
    return handle;
  }
}
