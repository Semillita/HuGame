package dev.hugame.model.spec;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class ResolvedVertex {
	private final Vector3f position;

	private final Vector3f normal;

	private final Vector2f textureCoordinates;

	public ResolvedVertex(Vector3f position, Vector3f normal, Vector2f textureCoordinates) {
		this.position = position;
		this.normal = normal;
		this.textureCoordinates = textureCoordinates;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getNormal() {
		return normal;
	}

	public Vector2f getTextureCoordinates() {
		return textureCoordinates;
	}
}
