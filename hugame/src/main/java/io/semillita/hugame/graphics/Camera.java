package io.semillita.hugame.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

	private Matrix4f projectionMatrix, viewMatrix;
	public Vector3f position;
	
	public Camera(Vector3f position) {
		this.position = position;
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		adjustProjection();
	}
	
	public void adjustProjection() {
		projectionMatrix.identity();
		//projectionMatrix.ortho(0.0f, 1920, 0.0f, 1080, 0.0f, 100.0f);
//		projectionMatrix.setPerspectiveRect(1920, 1080, 0.1f, 500f);
		projectionMatrix.setPerspective(45, 1920 / 1080f, 0.1f, 1000f);
		
		Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
		viewMatrix.identity();
		viewMatrix.lookAt(position,
				//cameraFront.add(position),
				new Vector3f(0, 0, 0),
				cameraUp);
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
		adjustProjection();
	}
	
}
