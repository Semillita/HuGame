package dev.hugame.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/** A camera with a perspective projection */
public non-sealed class PerspectiveCamera extends Camera {

	public Vector3f position;
	public Vector3f direction;
	public Vector3f up;

	/** Creates a perspective camera with the given position. */
	public PerspectiveCamera(Vector3f position) {
		this(position, new Vector3f(0, 0, -1), new Vector3f(0, 1, 0));
	}

	/** Creates a perspective camera with the given view spec. */
	public PerspectiveCamera(Vector3f position, Vector3f direction, Vector3f up) {
		this.position = position;
		this.direction = direction;
		this.up = up;
		update();
	}

	/** Returns a new vector representing this camera's position. */
	public Vector3f getPosition() {
		return new Vector3f(position);
	}
	
	/** Returns a new vector representing this camera's direction. */
	public Vector3f getDirection() {
		return new Vector3f(direction);
	}
	
	/** Returns a new vector representing this camera's up direction. */
	public Vector3f getUp() {
		return new Vector3f(up);
	}
	
	/** Sets the position of this camera to the values of the supplied vector. */
	public void setPosition(Vector3f position) {
		this.position = new Vector3f(position.x, position.y, position.z);
	}
	
	/** Sets the direction of this camera to the values of the supplied vector. */
	public void setDirection(Vector3f direction) {
		this.direction = new Vector3f(direction.x, direction.y, direction.z);
	}

	/** Sets the up direction of this camera to the values of the supplied vector. */
	public void setUp(Vector3f up) {
		this.up = new Vector3f(up.x, up.y, up.z);
	}

	/** Sets the direction of this camera to point at the supplied target vector. */
	public void lookAt(Vector3f target) {
		target.sub(position, direction);
	}

	@Override
	public void update() {
		projectionMatrix = new Matrix4f().identity().setPerspective(45, 1920 / 1080f, 0.1f, 10000f);
		viewMatrix = new Matrix4f().identity().lookAt(position, new Vector3f(position).add(direction.normalize()), up);
	}

}
