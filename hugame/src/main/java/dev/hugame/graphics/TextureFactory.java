package dev.hugame.graphics;

import dev.hugame.graphics.Texture;

public interface TextureFactory {
	/**
	 * Creates a texture from the given bytes.
	 *
	 * @param resolvedTexture the resolved texture in the standard format
	 * @return a new texture
	 */
	Texture createTexture(ResolvedTexture resolvedTexture);
}
