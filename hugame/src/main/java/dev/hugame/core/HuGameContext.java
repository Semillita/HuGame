package dev.hugame.core;

/** A context used to provide objects specific to the platform and API. */
public interface HuGameContext {

	/** Returns the general-purpose graphics instance. */
	public Graphics getGraphics();

	/**
	 * Returns the window object representing a desktop OS window or (in the future)
	 * a mobile screen.
	 */
	public Window getWindow();

	/**
	 * Returns an interface for retrieving input states and listening to input
	 * events.
	 */
	public Input getInput();

}
