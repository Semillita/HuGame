package dev.hugame.vulkan.image;

import dev.hugame.vulkan.core.VulkanGraphics;

import static org.lwjgl.vulkan.VK10.*;

// TODO: Make this belong to the swap chain
public class DepthBuffer {
	public static DepthBuffer create(VulkanGraphics graphics, int width, int height) {
		final var format = VK_FORMAT_D32_SFLOAT;

		var image = ImageUtils.createImage(graphics, width, height, format, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
		var imageView = VulkanImageView.create(graphics, image, format, VK_IMAGE_ASPECT_DEPTH_BIT);

		return new DepthBuffer(image, imageView);
	}

	private final VulkanImage image;
	private final VulkanImageView imageView;

	private DepthBuffer(VulkanImage image, VulkanImageView imageView) {
		this.image = image;
		this.imageView = imageView;
	}

	public VulkanImage getImage() {
		return image;
	}

	public VulkanImageView getImageView() {
		return imageView;
	}
}
