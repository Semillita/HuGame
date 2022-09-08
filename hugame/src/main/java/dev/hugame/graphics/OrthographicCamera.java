package dev.hugame.graphics;

import java.awt.Dimension;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public non-sealed class OrthographicCamera extends Camera {

	protected Vector3f position;
	protected Vector3f direction;
	protected Vector3f up;
	protected Dimension viewportSize;

	public OrthographicCamera(Vector3f position, int viewportWidth, int viewportHeight) {
		this(position, new Vector3f(0, 0, -1), new Vector3f(0, 1, 0), viewportWidth, viewportHeight);
	}

	public OrthographicCamera(Vector3f position, Vector3f direction, Vector3f up, int viewportWidth,
			int viewportHeight) {
		this.position = position;
		this.direction = direction;
		this.up = up;
		viewportSize = new Dimension(viewportWidth, viewportHeight);
		update();
	}

	@Override
	public void update() {
		super.projectionMatrix = new Matrix4f().identity().ortho(-viewportSize.width / 2f, viewportSize.width / 2f,
				-viewportSize.height / 2f, viewportSize.height / 2f, 0.0f, 100.0f);

		Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
		super.viewMatrix = new Matrix4f().identity().lookAt(position, cameraFront.add(position), cameraUp);
	}

}
