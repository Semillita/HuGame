package dev.hugame.environment;

import java.util.Arrays;

import org.joml.Vector3f;

import dev.hugame.util.ByteSerializer;

/** A sun-like light without a given position. */
public non-sealed class DirectionalLight extends Light {
	
	public static final int SIZE_IN_BYTES = 16 * Float.BYTES;

	private Vector3f direction;
	
	private Vector3f ambient;
	private Vector3f diffuse;
	private Vector3f specular;
	
	private float strength;

	/** Returns the direction of the light source. */
	public Vector3f getDirection() {
		return direction;
	}

	@Override
	public byte[] getBytes() {
		var directionBytes = ByteSerializer.toBytes(direction);
		var ambientBytes = ByteSerializer.toBytes(ambient);
		var diffuseBytes = ByteSerializer.toBytes(diffuse);
		var specularBytes = ByteSerializer.toBytes(specular);
		var strengthBytes = ByteSerializer.toBytes(strength);
		var bullshitBytes = new byte[] {0, 0, 0, 0};
		
		return ByteSerializer.squash(Arrays.asList(directionBytes, strengthBytes, ambientBytes, 
				bullshitBytes, diffuseBytes, bullshitBytes, specularBytes, bullshitBytes));
	}

}
