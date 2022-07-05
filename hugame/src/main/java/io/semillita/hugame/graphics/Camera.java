package io.semillita.hugame.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

	protected Matrix4f projectionMatrix, viewMatrix;
	public Vector3f position;
	
	public Camera(Vector3f position) {
		this.position = position;
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		adjustProjection();
		lookAt(position, new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
	}
	
	public void adjustProjection() {
		projectionMatrix.identity();
		projectionMatrix.setPerspective(45, 1920 / 1080f, 0.1f, 1000f);
	}
	
	public void lookInDirection(Vector3f direction) {
		lookInDirection(position, direction, new Vector3f(0, 1, 0));
	}
	
	public void lookInDirection(Vector3f direction, Vector3f up) {
		lookInDirection(position, direction, up);
	}
	
	public void lookInDirection(Vector3f position, Vector3f direction, Vector3f up) {
		lookAt(position, new Vector3f(position).add(direction), up);
	}
	
	public void lookAt(Vector3f position, Vector3f target, Vector3f up) {
		var matrix = new Matrix4f().identity();
		matrix.lookAt(position, target, up);
		viewMatrix = matrix;
	}
	
	public void setDirection(Vector3f direction, Vector3f up) {
		final var matrix = new Matrix4f().identity();
		
		matrix.lookAlong(direction.x, direction.y, direction.z, up.x, up.y, up.z);
	}
	
	public Matrix4f getViewMatrix() {
		return this.viewMatrix;
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	public Vector3f getPosition() {
		return new Vector3f(position.x, position.y, position.z);
	}
	
	public void setPosition(Vector3f position) {
		this.position = new Vector3f(position.x, position.y, position.z);
	}
	
	// Not used, keeping for later
	private String vecString(Vector3f vec) {
		return "( " + vec.x + ", " + vec.y + ", " + vec.z + " )";
	}
	
}
