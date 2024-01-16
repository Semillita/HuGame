package dev.hugame.core;

import dev.hugame.graphics.TextureFactory;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.model.Model;
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

	/** Creates a 2D batch capable of batching 2D draw calls */
	Batch createBatch();

	void create();

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
}
