package io.semillita.hugame.graphics.material;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import org.joml.Vector3f;
import org.joml.Vector4f;

import io.semillita.hugame.graphics.Texture;

public class Material {
	
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
		byte[] bytes = new byte[16];
		
		//ByteBuffer.allocate(4).putFloat(ambientColor.x).putFlo
		
		int xBits =  Float.floatToIntBits(ambientColor.x);
		int yBits =  Float.floatToIntBits(ambientColor.y);
		int zBits =  Float.floatToIntBits(ambientColor.z);
		int wBits =  Float.floatToIntBits(ambientColor.w);
		
	    bytes[0] = (byte) (xBits >> 24);
	    bytes[1] = (byte) (xBits >> 16);
	    bytes[2] = (byte) (xBits >> 8);
	    bytes[3] = (byte) (xBits);
	    
	    bytes[4] = (byte) (yBits >> 24);
	    bytes[5] = (byte) (yBits >> 16);
	    bytes[6] = (byte) (yBits >> 8);
	    bytes[7] = (byte) (yBits);
	    
	    bytes[8] = (byte) (zBits >> 24);
	    bytes[9] = (byte) (zBits >> 16);
	    bytes[10] = (byte) (zBits >> 8);
	    bytes[11] = (byte) (zBits);
	    
	    bytes[12] = (byte) (wBits >> 24);
	    bytes[13] = (byte) (wBits >> 16);
	    bytes[14] = (byte) (wBits >> 8);
	    bytes[15] = (byte) (wBits);
		
		return bytes;
	}
	
	@Override
	public String toString() {
		return "<Material " + index + " - ambientColor: " + ambientColor.toString() + ">";
	}
}
