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

	/**
	 * Returns a byte array containing the bytes of a {@link Vector3f}.
	 * 
	 * @return the bytes of the vector, in order
	 */
	private byte[] vec3Bytes(Vector3f source) {
		byte[] bytes = new byte[3 * Float.BYTES];

		int xBits = Float.floatToIntBits(source.x);
		int yBits = Float.floatToIntBits(source.y);
		int zBits = Float.floatToIntBits(source.z);

		bytes[0] = (byte) (xBits >> 0);
		bytes[1] = (byte) (xBits >> 8);
		bytes[2] = (byte) (xBits >> 16);
		bytes[3] = (byte) (xBits >> 24);

		bytes[4] = (byte) (yBits >> 0);
		bytes[5] = (byte) (yBits >> 8);
		bytes[6] = (byte) (yBits >> 16);
		bytes[7] = (byte) (yBits >> 24);

		bytes[8] = (byte) (zBits >> 0);
		bytes[9] = (byte) (zBits >> 8);
		bytes[10] = (byte) (zBits >> 16);
		bytes[11] = (byte) (zBits >> 24);

		return bytes;
	}

}
