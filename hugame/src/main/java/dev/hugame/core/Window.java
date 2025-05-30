package dev.hugame.core;

import java.awt.Dimension;
import java.util.function.BiConsumer;

/** Interface for interacting with a window. */
public interface Window {

	/** Sets the visibility of the window */
	public void setVisible(boolean visible);

	/**
	 * Returns the current size of the window.
	 * 
	 * @return the size in pixels
	 */
	public Dimension getSize();

	/** Returns the width of the window. */
	public int getWidth();

	/** Returns the height of the window. */
	public int getHeight();

	/** Returns the x-position of the window. */
	public int getX();

	/** Returns the y-position of the window. */
	public int getY();

	/** Saves a listener for window resize events. */
	void addResizeListener(BiConsumer<Integer, Integer> listener);

	/**
	 * Makes the window icon appear as though it requests attention. Appearance
	 * details depend on OS.
	 */
	public void requestAttention();

	/** Destroys the window if possible. */
	public void destroy();

	/** Polls window events from OS. */
	public void pollEvents();

	/** Swaps the buffers of the window. */
	public void swapBuffers();

	/** Returns whether the window has been requested to close. */
	public boolean shouldClose();

	/** Sets whether the window is requested to close. */
	public void setShouldClose(boolean shouldClose);

	void setPosition(int x, int y);
}
