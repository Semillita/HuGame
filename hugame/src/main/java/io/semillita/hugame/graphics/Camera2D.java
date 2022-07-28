package io.semillita.hugame.graphics;

import java.awt.Dimension;
import java.awt.Point;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import io.semillita.hugame.core.HuGame;

public non-sealed class Camera2D extends Camera {

	private Vector2f position;
	private Dimension viewportSize;

	public Camera2D(Vector2f position, int viewportWidth, int viewportHeight) {
		this.position = position;
		this.viewportSize = new Dimension(viewportWidth, viewportHeight);
		update();
	}

	public Point screenToWorldCoords(int screenX, int screenY) {
		int screenWidth = HuGame.getWindow().getWidth();
		int screenHeight = HuGame.getWindow().getHeight();
		int worldX = (int) (screenX * (viewportSize.width / (float) screenWidth) + position.x - viewportSize.width / 2);
		int worldY = (int) (screenY * (viewportSize.height / (float) screenHeight) + position.y
				- viewportSize.height / 2);

		return new Point(worldX, worldY);
	}

	public Point screenToWorldCoords(Point point) {
		return screenToWorldCoords(point.x, point.y);
	}

	public Vector2f getPosition() {
		return new Vector2f(position.x, position.y);
	}

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

}
