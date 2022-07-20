package io.semillita.hugame.environment;

import org.joml.Vector3f;

public non-sealed class SpotLight extends Light {

	private Vector3f position, direction;
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getDirection() {
		return direction;
	}
	
}
