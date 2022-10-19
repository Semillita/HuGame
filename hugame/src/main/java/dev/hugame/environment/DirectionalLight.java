package dev.hugame.environment;

import java.util.Arrays;

import org.joml.Vector3f;

import dev.hugame.util.ByteSerializer;

/** A sun-like light without a given position. */
public non-sealed class DirectionalLight extends Light {
	public static final int SIZE_IN_BYTES = 8 * Float.BYTES;
	
	private Vector3f direction;
	private Vector3f color;
	
	private float strength;
	
	public DirectionalLight(Vector3f direction, Vector3f color, float strength) {
		this.direction = direction;
		this.color = color;
		this.strength = strength;
	}

	@Override
	public byte[] getBytes() {
		var directionBytes = ByteSerializer.toBytes(direction);
		var colorBytes = ByteSerializer.toBytes(color);
		var strengthBytes = ByteSerializer.toBytes(strength);
		var bullshitBytes = new byte[] {0, 0, 0, 0};
		
		return ByteSerializer.squash(Arrays.asList(directionBytes, strengthBytes, colorBytes, 
				bullshitBytes));
	}

}
