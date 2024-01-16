package dev.hugame.vulkan.core;

import dev.hugame.vulkan.sync.BinarySemaphore;
import dev.hugame.vulkan.sync.VulkanFence;

// TODO: Rename to SwapChainFrame, make the swap chain a more extensive object which holds these
// frames
public class InFlightFrame {
  private final BinarySemaphore imageAvailableSemaphore;
  private final BinarySemaphore imagePreparedForPresentingSemaphore;
  private final VulkanFence fence;

  public InFlightFrame(
      BinarySemaphore imageAvailableSemaphore,
      BinarySemaphore renderFinishedSemaphore,
      VulkanFence fence) {
    this.imageAvailableSemaphore = imageAvailableSemaphore;
    this.imagePreparedForPresentingSemaphore = renderFinishedSemaphore;
    this.fence = fence;
  }

  public BinarySemaphore getImageAvailableSemaphore() {
    return imageAvailableSemaphore;
  }

  public BinarySemaphore getImagePreparedForPresentingSemaphore() {
    return imagePreparedForPresentingSemaphore;
  }

  public VulkanFence getFence() {
    return fence;
  }
}
