package dev.hugame.environment;

import java.util.Arrays;

import org.joml.Vector3f;

import dev.hugame.util.ByteSerializer;

/** A light source that shines in all directions. */
public non-sealed class PointLight extends Light {

	public static final int SIZE_IN_BYTES = 16 * Float.BYTES;
	public static final int BYTES = 16 * Float.BYTES;

	private Vector3f position;

	private Vector3f color;
	
	private float strength;

	// TODO: Mutliply with some intensity value
	private final float constant;
	private final float linear;
	private final float quadratic;

	public PointLight(Vector3f position, Vector3f color, float strength) {
		this.position = position;
		this.color = color;

		this.strength = strength;

		this.constant = 1;
		this.linear = 0.20f;
		this.quadratic = 0.092f;
	}

	/** Returns the position of this light. */
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public byte[] getBytes() {
		var positionBytes = ByteSerializer.toBytes(position);
		var colorBytes = ByteSerializer.toBytes(color);
		var constantBytes = ByteSerializer.toBytes(constant);
		var linearBytes = ByteSerializer.toBytes(linear);
		var quadraticBytes = ByteSerializer.toBytes(quadratic);
		var strengthBytes = ByteSerializer.toBytes(strength);
		var bullshitBytes = new byte[] {0, 0, 0, 0};
		return ByteSerializer.squash(Arrays.asList(positionBytes, constantBytes, colorBytes, linearBytes,
				quadraticBytes, strengthBytes, bullshitBytes, bullshitBytes));
	}

}
