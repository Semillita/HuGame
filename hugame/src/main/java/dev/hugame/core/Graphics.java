package dev.hugame.core;

import dev.hugame.graphics.Texture;

/** A general interface to interact with objects specific to the graphics API */
public interface Graphics {

	/** Returns the graphics API with platform used in the running application */
	public GraphicsAPI getAPI();

	/** Returns the renderer used with the specific graphics API */
	public Renderer getRenderer();

	/**
	 * Creates a texture from the given bytes.
	 * 
	 * @param bytes the bytes representing the content of the file
	 * 
	 * @return a new texture
	 */
	public Texture getTexture(byte[] bytes);
	
	public void create();

	/** Clears the context's framebuffer with the given RGB value. */
	void clear(float red, float green, float blue);
}
