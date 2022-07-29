package io.semillita.hugame.graphics;

import java.awt.Dimension;
import java.awt.Point;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import io.semillita.hugame.core.HuGame;

public non-sealed class Camera2D extends Camera {

	private final Dimension minViewportSize;
	private Dimension viewportSize;
	private Vector2f position;

	
	public Camera2D(Vector2f position, Dimension minViewportSize) {
		this.position = position;
		this.minViewportSize = minViewportSize;
		
		updateViewport();
		update();
	}

	public Point screenToWorldCoords(int screenX, int screenY) {
		int screenWidth = HuGame.getWindow().getWidth();
		int screenHeight = HuGame.getWindow().getHeight();
		int worldX = (int) (screenX * (minViewportSize.width / (float) screenWidth) + position.x - minViewportSize.width / 2);
		int worldY = (int) (screenY * (minViewportSize.height / (float) screenHeight) + position.y
				- minViewportSize.height / 2);

		return new Point(worldX, worldY);
	}

	public Point screenToWorldCoords(Point point) {
		return screenToWorldCoords(point.x, point.y);
	}

	public Vector2f getPosition() {
		return new Vector2f(position.x, position.y);
	}

	public Dimension getViewportSize() {
		return viewportSize;
	}
	
	public void setPosition(Vector2f position) {
		this.position = new Vector2f(position.x, position.y);
	}

	@Override
	public void update() {
		System.out.println("Updating camera");
		System.out.println("minViewportSize: " + minViewportSize);
		System.out.println("viewportSize: " + viewportSize);
		super.projectionMatrix = new Matrix4f().identity().ortho(-viewportSize.width / 2f, viewportSize.width / 2f,
				-viewportSize.height / 2f, viewportSize.height / 2f, 0.0f, 100.0f);

		super.viewMatrix = new Matrix4f().identity().lookAt(new Vector3f(position.x, position.y, 1),
				new Vector3f(position.x, position.y, -1), new Vector3f(0.0f, 1.0f, 0.0f));
	}
	
	public void updateViewport() {
		System.out.println("-----------------------------------------------------------------------------------");
		var windowSize = HuGame.getWindow().getSize();
		var minViewportSizeRatio = minViewportSize.width / (float) minViewportSize.height;
		System.out.println("minViewportSizeRation: " + minViewportSizeRatio);
		System.out.println("Window width: " + windowSize.width);
		System.out.println("Window height: " + windowSize.height);
		var windowSizeRatio = windowSize.width / (float) windowSize.height;
		System.out.println("windowSizeRatio: " + windowSizeRatio);
		
		if (minViewportSizeRatio > windowSizeRatio) {
			viewportSize = new Dimension(minViewportSize.width, (int) (minViewportSize.width / windowSizeRatio));
		} else {
			viewportSize = new Dimension((int) (minViewportSize.height * windowSizeRatio), minViewportSize.height);
		}
	}

}
