package io.semillita.hugame.graphics.material;

import java.nio.ByteBuffer;

import org.joml.Vector4f;

import io.semillita.hugame.util.Bufferable;

public class Material implements Bufferable {

	public static final int SIZE_IN_BYTES = 4 * Float.BYTES;

	private final int index;
	private final Vector4f ambientColor;

	Material(MaterialCreateInfo createInfo, int index) {
		this.index = index;

		this.ambientColor = createInfo.ambientColor();
	}

	public int getIndex() {
		return index;
	}

	public Vector4f getAmbientColor() {
		return ambientColor;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[SIZE_IN_BYTES];

		int xBits = Float.floatToIntBits(ambientColor.x);
		int yBits = Float.floatToIntBits(ambientColor.y);
		int zBits = Float.floatToIntBits(ambientColor.z);
		int wBits = Float.floatToIntBits(ambientColor.w);

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

		bytes[12] = (byte) (wBits >> 0);
		bytes[13] = (byte) (wBits >> 8);
		bytes[14] = (byte) (wBits >> 16);
		bytes[15] = (byte) (wBits >> 24);

		return bytes;
	}

	public void putIntoBuffer(ByteBuffer buffer, int index) {
		for (var val : new float[] { ambientColor.x, ambientColor.y, ambientColor.z, ambientColor.w }) {
			buffer.putFloat(index, val);
			index += Float.BYTES;
		}
	}

	@Override
	public String toString() {
		return "<Material " + index + " - ambientColor: " + ambientColor.toString() + ">";
	}
}
