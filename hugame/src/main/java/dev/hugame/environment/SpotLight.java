package dev.hugame.environment;

import org.joml.Vector3f;

/**
 * A light with a given position that shines in a given direction inside a
 * cone-shaped frustum.
 */
public non-sealed class SpotLight extends Light {

	public static final int SIZE_IN_BYTES = 10 * Float.BYTES;

	private Vector3f position;
	private Vector3f direction;
	private Vector3f color;
	private float strength;

	public SpotLight(Vector3f position, Vector3f direction, Vector3f color, float strengths) {
		this.position = position;
		this.direction = direction;
		this.color = color;
		this.strength = strength;
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
		byte[] bytes = new byte[SIZE_IN_BYTES];

		var positionBytes = vec3Bytes(position);
		for (int i = 0; i < positionBytes.length; i++) {
			bytes[i] = positionBytes[i];
		}

		var directionBytes = vec3Bytes(direction);
		for (int i = 0; i < directionBytes.length; i++) {
			bytes[i + positionBytes.length] = directionBytes[i];
		}

		var colorBytes = vec3Bytes(color);
		for (int i = 0; i < colorBytes.length; i++) {
			bytes[i + positionBytes.length + directionBytes.length] = colorBytes[i];
		}

		return bytes;
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
