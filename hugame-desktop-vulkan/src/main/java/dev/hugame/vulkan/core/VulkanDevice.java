package dev.hugame.vulkan.core;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK13.*;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.lwjgl.vulkan.*;

public class VulkanDevice {
  private static final Set<String> EXTENSION_NAMES = Set.of("VK_KHR_swapchain", "VK_EXT_descriptor_indexing");

  public static VulkanDevice create(VulkanGraphics graphics) {
    var vkInstance = graphics.getInstance().get();

    try (var memoryStack = stackPush()) {
      var deviceCountBuffer = memoryStack.callocInt(1);
      vkEnumeratePhysicalDevices(vkInstance, deviceCountBuffer, null);

      var deviceCount = deviceCountBuffer.get(0);
      if (deviceCount == 0) {
        throw new RuntimeException("[HuGame] Failed to find physical device for Vulkan");
      }

      var physicalDeviceDetails = pickPhysicalDevice(graphics);
      var physicalDevice = physicalDeviceDetails.physicalDevice;
      var deviceSupport = physicalDeviceDetails.deviceSupport;

      var queueFamilyIndices = deviceSupport.queueFamilyIndices;

      var uniqueIndices = queueFamilyIndices.getUnique();
      var deviceQueueCreateInfos =
          VkDeviceQueueCreateInfo.calloc(uniqueIndices.length, memoryStack);
      for (int i = 0; i < uniqueIndices.length; i++) {
        var queueCreateInfo = deviceQueueCreateInfos.get(i);
        queueCreateInfo
            .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
            .queueFamilyIndex(uniqueIndices[i])
            .pQueuePriorities(memoryStack.floats(1));
      }

      var physicalDeviceFeatures =
          VkPhysicalDeviceFeatures.calloc(memoryStack).samplerAnisotropy(true);

      var logicalDeviceCreateInfo =
          VkDeviceCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
              .pQueueCreateInfos(deviceQueueCreateInfos)
              .pEnabledFeatures(physicalDeviceFeatures)
              .ppEnabledExtensionNames(
                  VulkanUtils.asPointerBuffer(
                      memoryStack, EXTENSION_NAMES))
              .pNext(
                  VkPhysicalDeviceVulkan12Features.calloc(memoryStack)
                      .sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES)
                      .shaderSampledImageArrayNonUniformIndexing(true)
                      .descriptorIndexing(true)
                      .timelineSemaphore(true));

      if (graphics.validationLayersEnabled()) {
        logicalDeviceCreateInfo.ppEnabledLayerNames(
            VulkanValidations.getValidationLayersAsPointerBuffer(memoryStack));
      }

      var logicalDevicePointerBuffer = memoryStack.callocPointer(1);
      if (vkCreateDevice(physicalDevice, logicalDeviceCreateInfo, null, logicalDevicePointerBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create logical device");
      }

      var logicalDevice =
          new VkDevice(logicalDevicePointerBuffer.get(0), physicalDevice, logicalDeviceCreateInfo);

      var graphicsQueuePointerBuffer = memoryStack.callocPointer(1);
      vkGetDeviceQueue(
          logicalDevice, queueFamilyIndices.graphicsFamily, 0, graphicsQueuePointerBuffer);
      // var graphicsQueue = new VkQueue(graphicsQueuePointerBuffer.get(0), logicalDevice);
      var graphicsQueue = GraphicsQueue.forDevice(logicalDevice, queueFamilyIndices.graphicsFamily);

      var presentQueuePointerBuffer = memoryStack.callocPointer(1);
      vkGetDeviceQueue(
          logicalDevice, queueFamilyIndices.presentFamily, 0, presentQueuePointerBuffer);
      // var presentQueue = new VkQueue(presentQueuePointerBuffer.get(0), logicalDevice);
      var presentQueue = PresentQueue.forDevice(logicalDevice, queueFamilyIndices.presentFamily);

      return new VulkanDevice(
          physicalDevice, logicalDevice, graphicsQueue, presentQueue, deviceSupport);
    }
  }

  private record PhysicalDeviceDetails(
      VkPhysicalDevice physicalDevice, DeviceSupport deviceSupport) {}

  private static PhysicalDeviceDetails pickPhysicalDevice(VulkanGraphics graphics) {
    var vkInstance = graphics.getInstance().get();

    try (var memoryStack = stackPush()) {
      var deviceCountBuffer = memoryStack.callocInt(1);
      vkEnumeratePhysicalDevices(vkInstance, deviceCountBuffer, null);

      var deviceCount = deviceCountBuffer.get(0);
      if (deviceCount == 0) {
        throw new RuntimeException("[HuGame] Failed to find physical device for Vulkan");
      }

      var physicalDevicesBuffer = memoryStack.mallocPointer(deviceCount);
      vkEnumeratePhysicalDevices(vkInstance, deviceCountBuffer, physicalDevicesBuffer);

      for (int i = 0; i < physicalDevicesBuffer.capacity(); i++) {
        var deviceHandle = physicalDevicesBuffer.get(i);
        var vkPhysicalDevice = new VkPhysicalDevice(deviceHandle, vkInstance);
        var support = getPhysicalDeviceSupport(vkPhysicalDevice, graphics.getSurface());

        if (support.isAdequate()) {
          return new PhysicalDeviceDetails(vkPhysicalDevice, support);
        }
      }

      throw new RuntimeException(
          String.format(
              "[HuGame] Failed to find suitable physical device among " + "%d existing devices",
              deviceCount));
    }
  }

  private static DeviceSupport getPhysicalDeviceSupport(
      VkPhysicalDevice vkPhysicalDevice, VulkanSurface surface) {
    try (var memoryStack = stackPush()) {
      var physicalDeviceProperties = VkPhysicalDeviceProperties.calloc(memoryStack);
      vkGetPhysicalDeviceProperties(vkPhysicalDevice, physicalDeviceProperties);

      var physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc(memoryStack);
      vkGetPhysicalDeviceFeatures(vkPhysicalDevice, physicalDeviceFeatures);

      var queueFamilyIndices = findQueueFamilyIndices(vkPhysicalDevice, surface);

      var supportedExtensions = findSupportedExtensions(vkPhysicalDevice);

      var support = new DeviceSupport();
      support.geometryShaderSupport = physicalDeviceFeatures.geometryShader();
      support.queueFamilyIndices = queueFamilyIndices;
      support.supportedExtensions = supportedExtensions;
      if (supportedExtensions.contains("VK_KHR_swapchain")) {
        support.swapChainSupport = getSwapChainSupport(vkPhysicalDevice, surface);
      }

      var limits = physicalDeviceProperties.limits();
      var maxSampledImages = limits.maxDescriptorSetSampledImages();

      support.limits = new DeviceLimits(maxSampledImages);

      return support;
    }
  }

  private static QueueFamilyIndices findQueueFamilyIndices(
      VkPhysicalDevice vkPhysicalDevice, VulkanSurface surface) {
    var queueFamilyIndices = new QueueFamilyIndices();

    try (var memoryStack = stackPush()) {
      var queueFamiliesBuffer =
          VulkanUtils.query(
              memoryStack,
              vkPhysicalDevice,
              VK10::vkGetPhysicalDeviceQueueFamilyProperties,
              VkQueueFamilyProperties::malloc);

      for (int index = 0; index < queueFamiliesBuffer.capacity(); index++) {
        if ((queueFamiliesBuffer.get(index).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
          queueFamilyIndices.graphicsFamily = index;
        }

        var presentSupportBuffer = memoryStack.callocInt(1);
        vkGetPhysicalDeviceSurfaceSupportKHR(
            vkPhysicalDevice, index, surface.getHandle(), presentSupportBuffer);

        if (presentSupportBuffer.get(0) == VK_TRUE) {
          queueFamilyIndices.presentFamily = index;
        }

        if (queueFamilyIndices.isComplete()) {
          break;
        }
      }

      return queueFamilyIndices;
    }
  }

  private static List<String> findSupportedExtensions(VkPhysicalDevice vkPhysicalDevice) {
    try (var memoryStack = stackPush()) {
      var extensionCountBuffer = memoryStack.callocInt(1);

      vkEnumerateDeviceExtensionProperties(
          vkPhysicalDevice, (String) null, extensionCountBuffer, null);

      var extensionCount = extensionCountBuffer.get(0);
      var extensionBuffer = VkExtensionProperties.calloc(extensionCount);

      vkEnumerateDeviceExtensionProperties(
          vkPhysicalDevice, (String) null, extensionCountBuffer, extensionBuffer);

      return IntStream.range(0, extensionBuffer.capacity())
          .mapToObj(extensionBuffer::get)
          .map(VkExtensionProperties::extensionNameString)
          .toList();
    }
  }

  private static DeviceSwapChainSupport getSwapChainSupport(
      VkPhysicalDevice vkPhysicalDevice, VulkanSurface surface) {
    try (var memoryStack = stackPush()) {
      var swapChainSupportDetails = new DeviceSwapChainSupport();
      var surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
      swapChainSupportDetails.surfaceCapabilities = surfaceCapabilities;

      vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
          vkPhysicalDevice, surface.getHandle(), surfaceCapabilities);

      var formatCountBuffer = memoryStack.callocInt(1);
      vkGetPhysicalDeviceSurfaceFormatsKHR(
          vkPhysicalDevice, surface.getHandle(), formatCountBuffer, null);

      var formatCount = formatCountBuffer.get(0);
      if (formatCount != 0) {
        var formatBuffer = VkSurfaceFormatKHR.calloc(formatCount);
        vkGetPhysicalDeviceSurfaceFormatsKHR(
            vkPhysicalDevice, surface.getHandle(), formatCountBuffer, formatBuffer);

        swapChainSupportDetails.formats =
            IntStream.range(0, formatCount).mapToObj(formatBuffer::get).toList();
      } else {
        swapChainSupportDetails.formats = List.of();
      }

      var presentModeCountBuffer = memoryStack.mallocInt(1);

      vkGetPhysicalDeviceSurfacePresentModesKHR(
          vkPhysicalDevice, surface.getHandle(), presentModeCountBuffer, null);

      var presentModeCount = presentModeCountBuffer.get(0);
      if (presentModeCount != 0) {
        var presentModeBuffer = memoryStack.callocInt(presentModeCount);
        vkGetPhysicalDeviceSurfacePresentModesKHR(
            vkPhysicalDevice, surface.getHandle(), presentModeCountBuffer, presentModeBuffer);

        presentModeBuffer.rewind();

        swapChainSupportDetails.presentModes =
            IntStream.range(0, presentModeCount).map(presentModeBuffer::get).toArray();
      }

      return swapChainSupportDetails;
    }
  }

  public static class QueueFamilyIndices {
    private Integer graphicsFamily;
    private Integer presentFamily;

    private boolean isComplete() {
      return graphicsFamily != null && presentFamily != null;
    }

    private int[] getUnique() {
      return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
    }

    public Integer getGraphicsFamily() {
      return graphicsFamily;
    }

    public Integer getPresentFamily() {
      return presentFamily;
    }
  }

  public static class DeviceLimits {
    private final int maxSampledImages;

    private DeviceLimits(int maxSampledImages) {
      this.maxSampledImages = maxSampledImages;
    }

    public int getMaxSampledImages() {
      return maxSampledImages;
    }
  }

  public static class DeviceSupport {
    private boolean geometryShaderSupport;
    private QueueFamilyIndices queueFamilyIndices;
    private List<String> supportedExtensions;
    private DeviceSwapChainSupport swapChainSupport;
    private DeviceLimits limits;

    public boolean isAdequate() {
      return geometryShaderSupport
          && queueFamilyIndices.isComplete()
          && supportedExtensions.contains("VK_KHR_swapchain")
          && swapChainSupport.isAdequate();
    }

    public QueueFamilyIndices getQueueFamilyIndices() {
      return queueFamilyIndices;
    }

    public DeviceSwapChainSupport getSwapChainSupport() {
      return swapChainSupport;
    }

    public DeviceLimits getLimits() {
      return limits;
    }
  }

  public static class DeviceSwapChainSupport {
    private VkSurfaceCapabilitiesKHR surfaceCapabilities;
    // TODO: Maybe keep own struct instead of native resource
    private List<VkSurfaceFormatKHR> formats;

    private int[] presentModes;

    public boolean isAdequate() {
      return !formats.isEmpty() && presentModes != null;
    }

    public VkSurfaceCapabilitiesKHR getSurfaceCapabilities() {
      return surfaceCapabilities;
    }

    public List<VkSurfaceFormatKHR> getFormats() {
      return formats;
    }

    public int[] getPresentModes() {
      return presentModes;
    }
  }

  private final VkPhysicalDevice physicalDevice;
  private final VkDevice logicalDevice;
  private final GraphicsQueue graphicsQueue;
  private final PresentQueue presentQueue;
  private final DeviceSupport support;

  private VulkanDevice(
      VkPhysicalDevice physicalDevice,
      VkDevice logicalDevice,
      GraphicsQueue graphicsQueue,
      PresentQueue presentQueue,
      DeviceSupport support) {
    this.physicalDevice = physicalDevice;
    this.logicalDevice = logicalDevice;
    this.graphicsQueue = graphicsQueue;
    this.presentQueue = presentQueue;
    this.support = support;
  }

  public VkPhysicalDevice getPhysical() {
    return physicalDevice;
  }

  public VkDevice getLogical() {
    return logicalDevice;
  }

  public GraphicsQueue getGraphicsQueue() {
    return graphicsQueue;
  }

  public PresentQueue getPresentQueue() {
    return presentQueue;
  }

  public DeviceSupport getSupport() {
    return support;
  }
}
