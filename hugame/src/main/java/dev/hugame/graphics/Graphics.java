package dev.hugame.graphics;

import dev.hugame.graphics.model.Model;
import dev.hugame.graphics.text.Font;
import dev.hugame.graphics.text.ResolvedFont;
import dev.hugame.model.spec.ResolvedModel;

/** A general interface to interact with objects specific to the graphics API */
public interface Graphics extends TextureFactory {

	/** Returns the graphics API with platform used in the running application */
	GraphicsAPI getAPI();

	/** Returns the renderer used with the specific graphics API */
	Renderer getRenderer();

	/** Creates a model implementation backed by the selected graphics API
	 *
	 * @param resolvedModel the resolved model data
	 * @return a model implementation */
	Model createModel(ResolvedModel resolvedModel);

	Font createFont(ResolvedFont resolvedFont);

	/** Creates a 2D batch capable of batching 2D draw calls */
	// TODO: Rename to createSpriteBatch
	Batch createBatch();

	// TODO: Move to interface GraphicsBackend to not be accessible to users through Graphics
	void create();

	// TODO: Move to interface GraphicsBackend to not be accessible to users through Graphics
	void endFrame();

	/** Swaps the graphic context's draw and display buffers */
	void swapBuffers();

	/** Clears the context's framebuffer with the given RGB value. */
	default void clear(float red, float green, float blue) {
		clear(red, green, blue, 1.0f);
	}

	/** Clears the context's framebuffer with the given RGBA value. */
	void clear(float red, float green, float blue, float alpha);

	/** Sets the color that the framebuffer is cleared to at the beginning of each frame */
	void setClearColor(float red, float green, float blue, float alpha);

	/**
	 * Retrieves the swap chain frame buffer for the current frame, i.e.
	 * the frame buffer that will be displayed to the screen at the end of
	 * the frame
	 *
	 * @return the current frame's swap chain frame buffer
	 * */
	FrameBuffer getSwapChainFrameBuffer();

	/**
	 * Creates a frame buffer that can be used as a render target
	 *
	 * @return a new frame buffer
	 * */
	FrameBuffer createFrameBuffer(int width, int height);

	/**
	 * Creates a texture from a frame buffer so that it can be sampled in a draw call
	 *
	 * @param frameBuffer the frame buffer to create a texture from
	 * @return a new texture
	 * */
	Texture createTextureFromFrameBuffer(FrameBuffer frameBuffer);
}
