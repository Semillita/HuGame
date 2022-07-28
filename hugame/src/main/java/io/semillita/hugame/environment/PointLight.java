package io.semillita.hugame.environment;

import java.util.Arrays;

import org.joml.Vector3f;

import io.semillita.hugame.util.ByteSerializer;

public non-sealed class PointLight extends Light {

	public static final int SIZE_IN_BYTES = 7 * Float.BYTES;
	
	private Vector3f position;
	private Vector3f color;
	private float intensity;

	public PointLight(Vector3f position, Vector3f color, float intensity) {
		this.position = position;
	}

	public Vector3f getPosition() {
		return position;
	}

	@Override
	public byte[] getBytes() {
		var positionBytes = ByteSerializer.toBytes(position);
		var colorBytes = ByteSerializer.toBytes(color);
		var intensityBytes = ByteSerializer.toBytes(intensity);
		return ByteSerializer.squash(Arrays.asList(positionBytes, colorBytes, intensityBytes));
	}
	
}
