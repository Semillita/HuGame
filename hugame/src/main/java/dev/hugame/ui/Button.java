package dev.hugame.ui;

import java.awt.Point;
import java.util.Optional;
import java.util.function.Function;

import dev.hugame.core.HuGame;

public abstract class Button implements GUIElement {

	private boolean hovered = false;
	private boolean pressed = false;
	private Optional<Point> lastMousePosition = Optional.empty();
	protected Optional<Function<Point, Point>> screenToWorldCoordinates = Optional.empty();

	public final boolean isHovered() {
		return hovered;
	}

	public final boolean isPressed() {
		return pressed;
	}

	@Override
	public final void setScreenToWorldCoordinateMapping(Function<Point, Point> screenToWorldCoordinates) {
		this.screenToWorldCoordinates = Optional.ofNullable(screenToWorldCoordinates);
	}

	public boolean isInside(int x, int y) {
		return false;
	}

	public void mouseDown() {
		var worldPosition = screenToWorldCoordinates.orElse(Function.identity())
				.apply(HuGame.getInput().getMousePosition());
		int x = worldPosition.x;
		int y = worldPosition.y;

		if (isInside(x, y)) {
			pressed = true;
			onPressed(x, y);
		}
	}

	public void mouseUp() {
		HuGame.getInput().acceptMousePosition((x, y) -> {
			onReleased(x, y);
			if (isInside(x, y) && pressed) {
				onClicked(x, y);
			}
			pressed = false;
		});
	}

	public void update() {
		var mousePosition = screenToWorldCoordinates.orElse(Function.identity())
				.apply(HuGame.getInput().getMousePosition());
		int x = mousePosition.x, y = mousePosition.y;
		var lastMousePosition = this.lastMousePosition.orElse(new Point(-1, -1));
		if (x != lastMousePosition.x || y != lastMousePosition.y) {
			mouseMoved(x, y);
		}
	}

	public void mouseMoved(int x, int y) {
		if (isInside(x, y)) {
			if (!hovered) {
				onHovered(x, y);
				hovered = true;
			}
			onMouseMoved(x, y);
		} else {
			hovered = false;
		}
	}
	
	public void onClicked(int x, int y) {
	}

	public void onHovered(int x, int y) {
	}

	public void onMouseMoved(int x, int y) {
	}

	public void onPressed(int x, int y) {
	}

	public void onReleased(int x, int y) {
	}
}
