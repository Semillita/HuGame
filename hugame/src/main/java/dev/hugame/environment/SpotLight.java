package dev.hugame.environment;

import org.joml.Vector3f;

public non-sealed class SpotLight extends Light {

	public static final int SIZE_IN_BYTES = 10 * Float.BYTES;
	
	private Vector3f position;
	private Vector3f direction;
	private Vector3f color;
	private float intensity;

	public SpotLight(Vector3f position, Vector3f direction, Vector3f color, float intensity) {
		this.position = position;
		this.direction = direction;
		this.color = color;
		this.intensity = intensity;
	}
	
	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getDirection() {
		return direction;
	}

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
