package dev.hugame.environment;

import java.util.Arrays;

import org.joml.Vector3f;

import dev.hugame.util.ByteSerializer;

/**
 * A light with a given position that shines in a given direction inside a
 * cone-shaped frustum.
 */
public non-sealed class SpotLight extends Light {

	public static final int SIZE_IN_BYTES = 20 * Float.BYTES;

	private Vector3f position;
	private Vector3f direction;
	
	private Vector3f color;
	
	private float strength;
	private float angle;
	private float outerAngle;

	private final float constant;
	private final float linear;
	private final float quadratic;
	
	public SpotLight(Vector3f position, Vector3f direction, Vector3f color, float strength, float angle) {
		this.position = position;
		this.direction = direction;
		this.color = color;
		this.strength = strength;
		this.angle = angle;
		this.outerAngle = angle + 0.05f;
		
		this.constant = 1;
		this.linear = 0.020f;
		this.quadratic = 0.0092f;
	}

	/** Returns the position of this light. */
	public Vector3f getPosition() {
		return position;
	}

	/** Returns the direction of this light. */
	public Vector3f getDirection() {
		return direction;
	}

	@Override
	public byte[] getBytes() {
		var positionBytes = ByteSerializer.toBytes(position);
		var directionBytes = ByteSerializer.toBytes(direction);
		var colorBytes = ByteSerializer.toBytes(color);
		var constantBytes = ByteSerializer.toBytes(constant);
		var linearBytes = ByteSerializer.toBytes(linear);
		var quadraticBytes = ByteSerializer.toBytes(quadratic);
		var strengthBytes = ByteSerializer.toBytes(strength);
		var cutOffBytes = ByteSerializer.toBytes((float) Math.cos(angle));
		var outerCutOffBytes = ByteSerializer.toBytes((float) Math.cos(outerAngle));
		var bullshitBytes = new byte[] {0, 0, 0, 0};
		
		return ByteSerializer.squash(Arrays.asList(positionBytes, constantBytes, directionBytes, linearBytes, 
				colorBytes, quadraticBytes, strengthBytes, cutOffBytes, outerCutOffBytes, bullshitBytes));
	}

}
