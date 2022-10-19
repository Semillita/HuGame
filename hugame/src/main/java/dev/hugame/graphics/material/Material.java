package dev.hugame.graphics.material;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.joml.Vector3f;
import org.joml.Vector4f;

import dev.hugame.util.Bufferable;
import dev.hugame.util.ByteSerializer;

/** A render material that describes different lighting effects. */
public class Material implements Bufferable {

	public static final int SIZE_IN_BYTES = 16 * Float.BYTES;

	private final int index;
	private final Vector3f ambientColor;
	private final Vector3f diffuseColor;
	private final Vector3f specularColor;
	private final float shininess;
	private final int albedoMapID;
	private final int normalMapID;
	private final int specularMapID;
	private final int albedoMapSlice;
	private final int normalMapSlice;
	private final int specularMapSlice;

	@Deprecated
	public Material(MaterialCreateInfo createInfo, int index) {
		this.index = index;

		this.ambientColor = createInfo.ambientColor();
		this.diffuseColor = createInfo.diffuseColor();
		this.specularColor = createInfo.specularColor();
		this.shininess = createInfo.shininess();
		this.albedoMapID = -1;
		this.normalMapID = -1;
		this.specularMapID = -1;
		this.albedoMapSlice = -1;
		this.normalMapSlice = -1;
		this.specularMapSlice = -1;
	}

	public Material(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float shininess,
			int albedoMapID, int normalMapID, int specularMapID, int albedoMapIndex, int normalMapIndex, int specularMapIndex, int index) {
//		System.out.println("------- Creating material with index " + index + " --------");

		this.index = index;

		this.ambientColor = ambientColor;
		this.diffuseColor = diffuseColor;
		this.specularColor = specularColor;
		this.shininess = shininess;
		this.albedoMapID = albedoMapID;
		this.normalMapID = normalMapID;
		this.specularMapID = specularMapID;
		this.albedoMapSlice = albedoMapIndex;
		this.normalMapSlice = normalMapIndex;
		this.specularMapSlice = specularMapIndex;
	}

	// TODO: Make this getGlobalIndex, indicating that it's used to index into material buffer
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
		var ambientBytes = ByteSerializer.toBytes(ambientColor);
		var diffuseBytes = ByteSerializer.toBytes(diffuseColor);
		var specularBytes = ByteSerializer.toBytes(specularColor);
		var shininessBytes = ByteSerializer.toBytes(shininess);
		var albedoMapIDBytes = ByteSerializer.toBytes(albedoMapID);
		var normalMapIDBytes = ByteSerializer.toBytes(normalMapID);
		var specularMapIDBytes = ByteSerializer.toBytes(specularMapID);
		var albedoMapSliceBytes = ByteSerializer.toBytes(albedoMapSlice);
		var normalMapSliceBytes = ByteSerializer.toBytes(normalMapSlice);
		var specularMapSliceBytes = ByteSerializer.toBytes(specularMapSlice);
		var bullshitBytes = new byte[] { 0, 0, 0, 0 };

		return ByteSerializer.squash(Arrays.asList(ambientBytes, albedoMapIDBytes, diffuseBytes, normalMapIDBytes,
				specularBytes, specularMapIDBytes, shininessBytes, albedoMapSliceBytes, normalMapSliceBytes, specularMapSliceBytes));
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
