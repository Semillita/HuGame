package dev.hugame.environment;

import java.util.Arrays;

import org.joml.Vector3f;

import dev.hugame.util.ByteSerializer;

public non-sealed class PointLight extends Light {

	public static final int SIZE_IN_BYTES = 15 * Float.BYTES;
	
	private Vector3f position;
	
	private Vector3f ambient;
	private Vector3f diffuse;
	private Vector3f specular;
	private float strength;

	// TODO: Mutliply with some intensity value
	private final float constant;
	private final float linear;
	private final float quadratic;
	
	public PointLight(Vector3f position, Vector3f color, float strength) {
		this.position = position;
		this.ambient = color;
		this.diffuse = color;
		this.specular = color;
		
		this.strength = strength;
		
		this.constant = 1;
		this.linear = 0.20f;
		this.quadratic = 0.092f;
	}

	public Vector3f getPosition() {
		return position;
	}

	@Override
	public byte[] getBytes() {
		var positionBytes = ByteSerializer.toBytes(position);
		var ambientBytes = ByteSerializer.toBytes(ambient);
		var diffuseBytes = ByteSerializer.toBytes(diffuse);
		var specularBytes = ByteSerializer.toBytes(specular);
		var constantBytes = ByteSerializer.toBytes(constant);
		var linearBytes = ByteSerializer.toBytes(linear);
		var quadraticBytes = ByteSerializer.toBytes(quadratic);
		var strengthBytes = ByteSerializer.toBytes(strength);
		return ByteSerializer.squash(Arrays.asList(positionBytes, constantBytes, ambientBytes, linearBytes, diffuseBytes, quadraticBytes, specularBytes,
				strengthBytes));
	}
	
}
