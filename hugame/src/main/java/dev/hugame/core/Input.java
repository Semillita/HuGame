package dev.hugame.core;

import java.awt.Point;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.hugame.input.Key;

public interface Input {

	public Point getMousePosition();
	
	public boolean isKeyPressed(Key key);
	
	public void acceptMousePosition(BiConsumer<Integer, Integer> consumer);
	
	public void setMouseButtonListener(Consumer<MouseEvent> listener);
	
	public void setKeyListener(BiConsumer<Key, KeyAction> listener);
	
	public static enum KeyAction {
		PRESS, RELEASE;
	}
	
	public static enum MouseAction {
		PRESS, RELEASE;
	}
	
	public static record MouseEvent(int x, int y, MouseAction action, int button) {}
}
