package dev.hugame.core;

import dev.hugame.context.TextureFactory;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Texture;
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
	void clear(float red, float green, float blue);
}
