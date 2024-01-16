package dev.hugame.vulkan.texture;

import dev.hugame.graphics.Texture;
import dev.hugame.util.ImageLoader;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.image.ImageUtils;
import dev.hugame.vulkan.image.VulkanImage;
import dev.hugame.vulkan.image.VulkanImageSampler;
import dev.hugame.vulkan.image.VulkanImageView;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanTexture implements Texture {
	// TODO: Let this class hold an inner object representing the vulkan
	//       texture and whose type varies depending on the usage
	//       I.e. private final VulkanTextureBase implementation;
	//       That makes this class' implementation only one possible
	//       implementation (static, no re-draw)
	public static VulkanTexture create(VulkanGraphics graphics, byte[] content) {
		var imageData = ImageLoader.read(content, 4);

		var image = ImageUtils.createImage(graphics, imageData);
		var imageView = VulkanImageView.create(graphics, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT);
		var imageSampler = VulkanImageSampler.create(graphics);

		return new VulkanTexture(image, imageView, imageSampler);
	}

	private final VulkanImage image;
	private final VulkanImageView imageView;
	private final VulkanImageSampler imageSampler;

	private VulkanTexture(VulkanImage image, VulkanImageView imageView, VulkanImageSampler imageSampler) {
		this.image = image;
		this.imageView = imageView;
		this.imageSampler = imageSampler;
	}

	public VulkanImage getImage() {
		return image;
	}

	public VulkanImageView getImageView() {
		return imageView;
	}

	public VulkanImageSampler getImageSampler() {
		return imageSampler;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getSlice() {
		return 0;
	}
}
