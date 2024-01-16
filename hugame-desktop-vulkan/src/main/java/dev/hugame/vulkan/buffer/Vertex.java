package dev.hugame.vulkan.buffer;

import dev.hugame.vulkan.core.VertexInput;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class Vertex {
	private final Vector3f position;
	private final Vector3f color;
	private final Vector2f textureCoordinates;

	public Vertex(Vector3f position, Vector3f color, Vector2f textureCoordinates) {
		this.position = position;
		this.color = color;
		this.textureCoordinates = textureCoordinates;
	}

	public void writeInto(ByteBuffer buffer) {
		buffer.putFloat(position.x);
		buffer.putFloat(position.y);
		buffer.putFloat(position.z);

		buffer.putFloat(color.x);
		buffer.putFloat(color.y);
		buffer.putFloat(color.z);

		buffer.putFloat(textureCoordinates.x);
		buffer.putFloat(textureCoordinates.y);
	}
}
