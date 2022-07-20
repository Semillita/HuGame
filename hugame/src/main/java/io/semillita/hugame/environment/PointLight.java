package io.semillita.hugame.environment;

import org.joml.Vector3f;

public non-sealed class PointLight extends Light {

	private Vector3f position;
	
	public PointLight(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
}
