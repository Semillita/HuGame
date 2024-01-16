package dev.hugame.core;

import dev.hugame.environment.Environment;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.model.Model;
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
	 * Flushes the queue of models with their respective transforms and materials.
	 */
	void flush();

	/** Returns the perspective camera used to render the scene. */
	PerspectiveCamera getCamera();

	/** Updates the render environment stored on the GPU. */
	void updateEnvironment(Environment environment);
}
