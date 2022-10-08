package dev.hugame.core;

import dev.hugame.core.graphics.Model;
import dev.hugame.environment.Environment;
import dev.hugame.graphics.GLBatch;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.util.Transform;

/** General-purpose interface for all types of render calls. */
public interface Renderer {

	/** Initializes the render. */
	public void create();

	/**
	 * Adds a model to the list of instances to be rendered.
	 * 
	 * @param model     the model to render
	 * @param transform the transform to render the model with
	 * @param material  the material to render the model with
	 */
	public void draw(Model model, Transform transform, Material material);

	/**
	 * Flushes the queue of models with their respective transforms and materials.
	 */
	public void flush();

	/** Returns the perspective camera used to render the scene. */
	public PerspectiveCamera getCamera();

	/** Updates the render environment stored on the GPU. */
	public void updateEnvironment(Environment environment);

	/** Renders a given batch of 2D draw calls. */
	public void renderBatch(GLBatch batch);
}
