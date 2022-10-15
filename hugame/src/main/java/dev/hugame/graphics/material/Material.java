package dev.hugame.graphics.material;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.joml.Vector3f;
import org.joml.Vector4f;

import dev.hugame.util.Bufferable;
import dev.hugame.util.ByteSerializer;

/** A render material that describes different lighting effects. */
public class Material implements Bufferable {

	public static final int SIZE_IN_BYTES = 12 * Float.BYTES;

	private final int index;
	private final Vector3f ambientColor;
	private final Vector3f diffuseColor;
	private final Vector3f specularColor;
	private final float shininess;

	Material(MaterialCreateInfo createInfo, int index) {
		this.index = index;

		this.ambientColor = createInfo.ambientColor();
		this.diffuseColor = createInfo.diffuseColor();
		this.specularColor = createInfo.specularColor();
		this.shininess = createInfo.shininess();
	}

	/** Returns the index of this material among all materials. */
	public int getIndex() {
		return index;
	}

	/** Returns the ambient color of this material. */
	public Vector3f getAmbientColor() {
		return ambientColor;
	}

	@Override
	public byte[] getBytes() {
//		byte[] bytes = new byte[SIZE_IN_BYTES];
//
//		int xBits = Float.floatToIntBits(ambientColor.x);
//		int yBits = Float.floatToIntBits(ambientColor.y);
//		int zBits = Float.floatToIntBits(ambientColor.z);
//
//		bytes[0] = (byte) (xBits >> 0);
//		bytes[1] = (byte) (xBits >> 8);
//		bytes[2] = (byte) (xBits >> 16);
//		bytes[3] = (byte) (xBits >> 24);
//
//		bytes[4] = (byte) (yBits >> 0);
//		bytes[5] = (byte) (yBits >> 8);
//		bytes[6] = (byte) (yBits >> 16);
//		bytes[7] = (byte) (yBits >> 24);
//
//		bytes[8] = (byte) (zBits >> 0);
//		bytes[9] = (byte) (zBits >> 8);
//		bytes[10] = (byte) (zBits >> 16);
//		bytes[11] = (byte) (zBits >> 24);
//
//		return bytes;
		
		var ambientBytes = ByteSerializer.toBytes(ambientColor);
		var diffuseBytes = ByteSerializer.toBytes(diffuseColor);
		var specularBytes = ByteSerializer.toBytes(specularColor);
		var shininessBytes = ByteSerializer.toBytes(shininess);
		var bullshitBytes = new byte[] {0, 0, 0, 0};
		
		return ByteSerializer.squash(Arrays.asList(ambientBytes, shininessBytes, diffuseBytes, bullshitBytes, specularBytes, bullshitBytes));
	}

	/**
	 * Puts the bytes of this material into a buffer.
	 * 
	 * @param buffer the buffer to insert into
	 * @param index  the index to put the bytes at
	 */
	public void putIntoBuffer(ByteBuffer buffer, int index) {
		for (var val : new float[] { ambientColor.x, ambientColor.y, ambientColor.z }) {
			buffer.putFloat(index, val);
			index += Float.BYTES;
		}
	}

	@Override
	public String toString() {
		return "<Material " + index + " - ambientColor: " + ambientColor.toString() + ">";
	}
}
