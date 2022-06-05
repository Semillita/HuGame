package io.semillita.hugame.graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class OrthographicCamera extends Camera {

	public OrthographicCamera(Vector2f position) {
		super(new Vector3f(position.x, position.y, 1));
	}

	@Override
	public void adjustProjection() {
		super.projectionMatrix.identity();
		super.projectionMatrix.ortho(0.0f, 1920, 0.0f, 1080, 0.0f, 100.0f);
		
		Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
		super.viewMatrix.identity();
		super.viewMatrix.lookAt(super.position,
				cameraFront.add(super.position),
				cameraUp);
	}
	
}
