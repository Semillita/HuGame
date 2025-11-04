package dev.hugame.graphics;

import dev.hugame.environment.Environment;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.model.Model;
import dev.hugame.graphics.text.Font;
import dev.hugame.util.Transform;

/** General-purpose interface for all types of render calls. */
public interface Renderer {

	/** Initializes the render. */
	void create();

	/** Prepares the renderer for a new frame */
	void beginFrame();

	/** Finishes the current frame */
	void endFrame();

	/**
	 * Adds a model to the list of instances to be rendered.
	 * 
	 * @param model     the model to render
	 * @param transform the transform to render the model with
	 * @param material  the material to render the model with
	 */
	void draw(Model model, Transform transform, Material material);

	/**
	 * Adds a model to the list of instances to be rendered.
	 * 
	 * @param model     the model to render
	 * @param transform the transform to render the model with
	 */
	void draw(Model model, Transform transform);

	/**
	 * Draws a string of text.
	 *
	 * @param text the string to draw
	 * @param font the font to use
	 * @param fontSize the size of the
	 * */
	// TODO: Need a camera or Scene2D or something, either in this method or in some context
	void drawText(String text, Font font, int fontSize, int x, int y);

	/**
	 * Flushes the queue of models with their respective transforms and materials.
	 */
	void flushTextRenderer();

	/**
	 * Sets the render target for upcoming draw operations
	 *
	 * @param renderTarget the render target to use for draw operations
	 * */
	void setRenderTarget(RenderTarget renderTarget);

	/**
	 * Flushes the queue of models with their respective transforms and materials.
	 */
	void flush();

	/** Returns the perspective camera used to render the scene. */
	PerspectiveCamera getCamera();

	/** Updates the render environment stored on the GPU. */
	void updateEnvironment(Environment environment);
}
