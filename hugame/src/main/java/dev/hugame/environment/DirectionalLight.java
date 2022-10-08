package dev.hugame.environment;

import org.joml.Vector3f;

/** A sun-like light without a given position. */
public non-sealed class DirectionalLight extends Light {

	private Vector3f direction;

	/** Returns the direction of the light source. */
	public Vector3f getDirection() {
		return direction;
	}

	@Override
	public byte[] getBytes() {
		return null;
	}

}
