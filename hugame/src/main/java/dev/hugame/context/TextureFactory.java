package dev.hugame.context;

import dev.hugame.graphics.Texture;

public interface TextureFactory {
	/**
	 * Creates a texture from the given bytes.
	 *
	 * @param bytes the bytes representing the content of the file
	 * @return a new texture
	 */
	Texture createTexture(byte[] bytes);
}
