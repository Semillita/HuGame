package dev.hugame.environment;

import org.joml.Vector3f;

public non-sealed class DirectionalLight extends Light {

	private Vector3f direction;

	public Vector3f getDirection() {
		return direction;
	}

	@Override
	public byte[] getBytes() {
		return null;
	}

}
