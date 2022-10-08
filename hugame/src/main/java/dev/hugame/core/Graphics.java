package dev.hugame.core;

/** A general interface to interact with objects specific to the graphics API */
public interface Graphics {

	/** Returns the graphics API with platform used in the running application */
	public GraphicsAPI getAPI();

	/** Returns the renderer used with the specific graphics API */
	public Renderer getRenderer();
}
