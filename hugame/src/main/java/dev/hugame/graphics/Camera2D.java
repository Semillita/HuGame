package dev.hugame.graphics;

import java.awt.Dimension;
import java.awt.Point;

import dev.hugame.core.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import dev.hugame.core.HuGame;

/** A camera used to capture rendering of 2D images and shapes. */
public non-sealed class Camera2D extends Camera {
	// TODO: Make the camera not need to hold a reference to the window
	private final Window window;
	private final Dimension minViewportSize;
	private Dimension viewportSize;
	private Vector2f position;

	public Camera2D(Window window, Vector2f position, Dimension minViewportSize) {
		this.window = window;
		this.position = position;
		this.minViewportSize = minViewportSize;

		updateViewport();
		update();
	}

	/**
	 * Translates screen coordinates to a point in the world based on this camera's
	 * viewport.
	 * 
	 * @param screenX the x-coordinate on the screen
	 * @param screenY the y-coordinate on the screen
	 * @return the point in world coordinates
	 */
	public Point screenToWorldCoords(int screenX, int screenY) {
		int screenWidth = HuGame.getWindow().getWidth();
		int screenHeight = HuGame.getWindow().getHeight();
		int worldX = (int) (screenX * (minViewportSize.width / (float) screenWidth) + position.x
				- minViewportSize.width / 2);
		int worldY = (int) (screenY * (minViewportSize.height / (float) screenHeight) + position.y
				- minViewportSize.height / 2);

		return new Point(worldX, worldY);
	}

	/**
	 * Translates a point on the screen to a point in the world based on this
	 * camera's viewport.
	 * 
	 * @see Camera2D#screenToWorldCoords(int, int)
	 */
	public Point screenToWorldCoords(Point point) {
		return screenToWorldCoords(point.x, point.y);
	}

	/** Returns the position of this camera. */
	public Vector2f getPosition() {
		return new Vector2f(position.x, position.y);
	}

	/** Returns the size of this camera's viewport. */
	public Dimension getViewportSize() {
		return viewportSize;
	}

	/**
	 * Sets the position of this camera to the coordinates of a given
	 * {@link Vector2f}.
	 */
	public void setPosition(Vector2f position) {
		this.position = new Vector2f(position.x, position.y);
	}

	@Override
	public void update() {
		super.projectionMatrix = new Matrix4f().identity().ortho(-viewportSize.width / 2f, viewportSize.width / 2f,
				-viewportSize.height / 2f, viewportSize.height / 2f, 0.0f, 100.0f);

		super.viewMatrix = new Matrix4f().identity().lookAt(new Vector3f(position.x, position.y, 1),
				new Vector3f(position.x, position.y, -1), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	/**
	 * Updates this camera's viewport based on potential changes in window
	 * resolution.
	 */
	public void updateViewport() {
		var windowSize = window.getSize();
		var minViewportSizeRatio = minViewportSize.width / (float) minViewportSize.height;
		var windowSizeRatio = windowSize.width / (float) windowSize.height;

		if (minViewportSizeRatio > windowSizeRatio) {
			viewportSize = new Dimension(minViewportSize.width, (int) (minViewportSize.width / windowSizeRatio));
		} else {
			viewportSize = new Dimension((int) (minViewportSize.height * windowSizeRatio), minViewportSize.height);
		}
	}

}
