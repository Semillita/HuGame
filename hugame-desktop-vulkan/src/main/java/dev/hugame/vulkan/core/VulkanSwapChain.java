package dev.hugame.vulkan.core;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.util.Logger;
import dev.hugame.vulkan.image.DepthBuffer;
import dev.hugame.vulkan.types.ImageViewType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.lwjgl.vulkan.*;

public class VulkanSwapChain {
  private static final int UNSIGNED_INTEGER_MAX_VALUE = 0xFFFFFFFF;

  // TODO: Introduce some VulkanSwapChainFactory class
  public static VulkanSwapChain create(VulkanGraphics graphics, long windowHandle) {
    var device = graphics.getDevice();
    var swapChainSupport = device.getSupport().getSwapChainSupport();
    var surfaceCapabilities = swapChainSupport.getSurfaceCapabilities();

    var surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.getFormats());
    var presentMode = chooseSwapPresentMode(swapChainSupport.getPresentModes());
    var extent = chooseSwapExtent(surfaceCapabilities, windowHandle);

    var imageCount = pickImageCount(surfaceCapabilities);

    var format = surfaceFormat.format();

    try (var memoryStack = stackPush()) {
      var swapChainCreateInfo =
          VkSwapchainCreateInfoKHR.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
              .surface(graphics.getSurface().getHandle())
              .minImageCount(imageCount)
              .imageFormat(format)
              .imageColorSpace(surfaceFormat.colorSpace())
              .imageExtent(extent)
              .imageArrayLayers(1)
              .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT);

      var queueFamilyIndices = device.getSupport().getQueueFamilyIndices();

      var graphicsFamily = queueFamilyIndices.getGraphicsFamily();
      var presentFamily = queueFamilyIndices.getPresentFamily();

      if (!Objects.equals(graphicsFamily, presentFamily)) {
        swapChainCreateInfo
            .imageSharingMode(VK_SHARING_MODE_CONCURRENT)
            .queueFamilyIndexCount(2)
            .pQueueFamilyIndices(memoryStack.ints(graphicsFamily, presentFamily));
      } else {
        swapChainCreateInfo
            .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
            .queueFamilyIndexCount(0)
            .pQueueFamilyIndices(null);
      }

      swapChainCreateInfo
          .preTransform(surfaceCapabilities.currentTransform())
          .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
          .presentMode(presentMode)
          .clipped(true)
          .oldSwapchain(VK_NULL_HANDLE);

      var swapChainHandleBuffer = memoryStack.callocLong(1);
      if (vkCreateSwapchainKHR(
              device.getLogical(), swapChainCreateInfo, null, swapChainHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create Vulkan swap chain");
      }

      var swapChainHandle = swapChainHandleBuffer.get(0);

      var logicalDevice = graphics.getDevice().getLogical();

      var swapChainImageCountBuffer = memoryStack.callocInt(1);
      vkGetSwapchainImagesKHR(logicalDevice, swapChainHandle, swapChainImageCountBuffer, null);

      var swapChainImageCount = swapChainImageCountBuffer.get(0);
      var swapChainImageHandleBuffer = memoryStack.callocLong(swapChainImageCount);
      vkGetSwapchainImagesKHR(
          logicalDevice, swapChainHandle, swapChainImageCountBuffer, swapChainImageHandleBuffer);

      var swapChainImageHandles =
          IntStream.range(0, swapChainImageCount)
              .mapToObj(swapChainImageHandleBuffer::get)
              .toList();

      var swapChainImageViewHandles = new ArrayList<Long>();

      for (int i = 0; i < swapChainImageCount; i++) {
        var imageViewCreateInfo =
            VkImageViewCreateInfo.calloc(memoryStack)
                .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(swapChainImageHandles.get(i))
                .viewType(ImageViewType._2D.getValue())
                .format(format)
                .components(
                    components ->
                        components
                            .r(VK_COMPONENT_SWIZZLE_IDENTITY)
                            .g(VK_COMPONENT_SWIZZLE_IDENTITY)
                            .b(VK_COMPONENT_SWIZZLE_IDENTITY)
                            .a(VK_COMPONENT_SWIZZLE_IDENTITY))
                .subresourceRange(
                    subresourceRange ->
                        subresourceRange
                            .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                            .baseMipLevel(0)
                            .levelCount(1)
                            .baseArrayLayer(0)
                            .layerCount(1));

        var imageViewHandleBuffer = memoryStack.callocLong(1);
        if (vkCreateImageView(logicalDevice, imageViewCreateInfo, null, imageViewHandleBuffer)
            != VK_SUCCESS) {
          throw new RuntimeException("[HuGame] Failed to create Vulkan image view");
        }

        var imageViewHandle = imageViewHandleBuffer.get(0);
        swapChainImageViewHandles.add(imageViewHandle);
      }

      var depthBuffer = DepthBuffer.create(graphics, extent.width(), extent.height());

      return new VulkanSwapChain(
          swapChainHandleBuffer.get(0),
          swapChainImageHandles,
          swapChainImageViewHandles,
          surfaceFormat,
          extent,
          depthBuffer);
    }
  }

  private static VkSurfaceFormatKHR chooseSwapSurfaceFormat(
      List<VkSurfaceFormatKHR> availableFormats) {
    if (availableFormats.isEmpty()) {
      throw new RuntimeException("[HuGame] Failed to find surface format");
    }

    return availableFormats.stream()
        .filter(
            format ->
                format.format() == VK_FORMAT_B8G8R8A8_SRGB
                    && format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
        .findFirst()
        .orElseGet(() -> availableFormats.get(0));
  }

  private static int chooseSwapPresentMode(int[] availablePresentModes) {
    return Arrays.stream(availablePresentModes)
        .filter(presentMode -> presentMode == VK_PRESENT_MODE_MAILBOX_KHR)
        .findFirst()
        .orElse(VK_PRESENT_MODE_FIFO_KHR);
  }

  /*
   *  TODO: The window parameter will need to be of some other type that's inherited
   *  by both the GLFW window implementation and the surface making up a part of the
   *  editor window, in other to support in-editor vulkan rendering.
   * */
  private static VkExtent2D chooseSwapExtent(
      VkSurfaceCapabilitiesKHR capabilities, long windowHandle) {
    if (capabilities.currentExtent().width() != UNSIGNED_INTEGER_MAX_VALUE) {
      return capabilities.currentExtent();
    }

    try (var memoryStack = stackPush()) {
      var windowWidthBuffer = memoryStack.callocInt(1);
      var windowHeightBuffer = memoryStack.callocInt(1);

      glfwGetFramebufferSize(windowHandle, windowWidthBuffer, windowHeightBuffer);

      var width = windowWidthBuffer.get(0);
      var height = windowHeightBuffer.get(0);

      var minExtent = capabilities.minImageExtent();
      var maxExtent = capabilities.maxImageExtent();

      var actualWidth = clamp(minExtent.width(), maxExtent.width(), width);
      var actualHeight = clamp(minExtent.height(), maxExtent.height(), height);

      return VkExtent2D.malloc().set(actualWidth, actualHeight);
    }
  }

  private static int clamp(int min, int max, int value) {
    return Math.max(min, Math.min(max, value));
  }

  private static int pickImageCount(VkSurfaceCapabilitiesKHR surfaceCapabilities) {
    var minImageCount = surfaceCapabilities.minImageCount();
    var maxImageCount = surfaceCapabilities.maxImageCount();

    var imageCount = minImageCount + 1;

    if (maxImageCount != 0 && imageCount > maxImageCount) {
      imageCount = maxImageCount;
    }

    return imageCount;
  }

  private final long handle;
  // TODO: Make the swap chain hold a list of framebuffers, and the framebuffers hold their own
  // image handles
  //  Or at least make the swap chain hold lists of VulkanImage and VulkanImageView
  private final List<Long> imageHandles;
  private final List<Long> imageViewHandles;
  private final VkSurfaceFormatKHR imageFormat;
  // TODO: Free this memory when this owning object is no longer used
  private final VkExtent2D swapChainExtent;
  private final DepthBuffer depthBuffer;

  private VulkanSwapChain(
      long handle,
      List<Long> imageHandles,
      List<Long> imageViewHandles,
      VkSurfaceFormatKHR imageFormat,
      VkExtent2D swapChainExtent,
      DepthBuffer depthBuffer) {
    this.handle = handle;
    this.imageHandles = imageHandles;
    this.imageViewHandles = imageViewHandles;
    this.imageFormat = imageFormat;
    this.swapChainExtent = swapChainExtent;
    this.depthBuffer = depthBuffer;
  }

  public long getHandle() {
    return handle;
  }

  public List<Long> getImageHandles() {
    return imageHandles;
  }

  public List<Long> getImageViewHandles() {
    return imageViewHandles;
  }

  public VkSurfaceFormatKHR getImageFormat() {
    return imageFormat;
  }

  public VkExtent2D getExtent() {
    return swapChainExtent;
  }

  public DepthBuffer getDepthBuffer() {
    return depthBuffer;
  }
}
