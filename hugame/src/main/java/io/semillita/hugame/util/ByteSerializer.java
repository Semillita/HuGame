package io.semillita.hugame.util;

import java.util.List;

import org.joml.Vector3f;

public class ByteSerializer {

	public static byte[] toBytes(Vector3f vec) {
		int x = Float.floatToIntBits(vec.x);
		int y = Float.floatToIntBits(vec.y);
		int z = Float.floatToIntBits(vec.z);
		return new byte[] {(byte) (x >> 0), (byte) (x >> 8), (byte) (x >> 16), (byte) (x >> 24),
				(byte) (y >> 0), (byte) (y >> 8), (byte) (y >> 16), (byte) (y >> 24),
				(byte) (z >> 0), (byte) (z >> 8), (byte) (z >> 16), (byte) (z >> 24)};
	}
	
	public static byte[] toBytes(float val) {
		int i = Float.floatToIntBits(val);
		return new byte[] {(byte) (i >> 0), (byte) (i), (byte) (i >> 16), (byte) (i >> 24)};
	}
	
	public static byte[] squash(List<byte[]> byteArrays) {
		int byteCount = byteArrays
				.stream()
				.mapToInt(byteArray -> byteArray.length)
				.sum();
		var bytes = new byte[byteCount];
		int index = 0;
		
		for (var byteArray : byteArrays) {
			for (var b : byteArray) {
				bytes[index] = b;
				index++;
			}
		}
		
		return bytes;
	}
	
}
