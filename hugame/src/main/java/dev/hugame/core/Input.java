package dev.hugame.core;

import java.awt.Point;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.hugame.input.Key;

/** Interface for interacting with input states and events. */
public interface Input {

	/**
	 * Returns the last mouse position on the window, where (0, 0) is the lower-left
	 * corner of the window.
	 */
	public Point getMousePosition();

	/** Returns whether a certain key is pressed. */
	public boolean isKeyPressed(Key key);

	/**
	 * Accepts a function to call with the current mouse coordinates.
	 * 
	 * @param consumer the consumer to run
	 */
	public void acceptMousePosition(BiConsumer<Integer, Integer> consumer);

	/**
	 * Saves a listener for mouse button input events.
	 * 
	 * @param listener the listener to save
	 */
	public void setMouseButtonListener(Consumer<MouseEvent> listener);

	/**
	 * Saves a listener for key input events.
	 * 
	 * @param listener the listener to save
	 */
	public void setKeyListener(BiConsumer<Key, KeyAction> listener);

	/** Enum to represent different action that can be done with a key. */
	public static enum KeyAction {
		PRESS, RELEASE;
	}

	/** Enum to represent different actions that can be done with a mouse button. */
	public static enum MouseAction {
		PRESS, RELEASE;
	}

	/** Container of data for a mouse button input event. */
	public static record MouseEvent(int x, int y, MouseAction action, int button) {
	}
}
