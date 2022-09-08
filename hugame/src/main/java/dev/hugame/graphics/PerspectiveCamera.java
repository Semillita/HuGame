package dev.hugame.graphics;

import java.awt.Dimension;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public non-sealed class PerspectiveCamera extends Camera {

	protected Vector3f position;
	protected Vector3f direction;
	protected Vector3f up;

	public PerspectiveCamera(Vector3f position) {
		this(position, new Vector3f(0, 0, -1), new Vector3f(0, 1, 0));
	}

	public PerspectiveCamera(Vector3f position, Vector3f direction, Vector3f up) {
		this.position = position;
		this.direction = direction;
		this.up = up;
		update();
	}

	public Vector3f getPosition() {
		return new Vector3f(position);
	}
	
	public Vector3f getDirection() {
		return new Vector3f(direction);
	}
	
	public Vector3f getUp() {
		return new Vector3f(up);
	}
	
	public void setPosition(Vector3f position) {
		this.position = new Vector3f(position.x, position.y, position.z);
	}
	
	public void setDirection(Vector3f direction) {
		this.direction = new Vector3f(direction.x, direction.y, direction.z);
	}

	public void setUp(Vector3f up) {
		this.up = new Vector3f(up.x, up.y, up.z);
	}

	public void lookAt(Vector3f target) {
		target.sub(position, direction);
	}

	@Override
	public void update() {
		projectionMatrix = new Matrix4f().identity().setPerspective(45, 1920 / 1080f, 0.1f, 1000f);
		viewMatrix = new Matrix4f().identity().lookAt(position, new Vector3f(position).add(direction.normalize()), up);
	}

}