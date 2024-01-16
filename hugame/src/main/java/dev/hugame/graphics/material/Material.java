package dev.hugame.graphics.material;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.joml.Vector3f;

import dev.hugame.util.Bufferable;
import dev.hugame.util.ByteSerializer;

/** A render material that describes different lighting effects. */
// TODO: Have the material record the Texture, not the index and layer, and resolve it through the graphics implementation.
//  That way, the graphics implementation can decide whether or not texture arrays are used.
public class Material implements Bufferable {

	public static final int SIZE_IN_BYTES = 16 * Float.BYTES;
	public static final int BYTES = 16 * Float.BYTES;

	private final int index;
	private final Vector3f ambientColor;
	private final Vector3f diffuseColor;
	private final Vector3f specularColor;
	private final float shininess;
	private final int albedoMapIndex;
	private final int normalMapIndex;
	private final int specularMapIndex;
	private final int albedoMapLayer;
	private final int normalMapLayer;
	private final int specularMapLayer;

	@Deprecated
	public Material(MaterialCreateInfo createInfo, int index) {
		this.index = index;

		this.ambientColor = createInfo.ambientColor();
		this.diffuseColor = createInfo.diffuseColor();
		this.specularColor = createInfo.specularColor();
		this.shininess = createInfo.shininess();
		this.albedoMapIndex = -1;
		this.normalMapIndex = -1;
		this.specularMapIndex = -1;
		this.albedoMapLayer = -1;
		this.normalMapLayer = -1;
		this.specularMapLayer = -1;
	}

	public Material(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float shininess,
			int albedoMapIndex, int normalMapIndex, int specularMapIndex, int albedoMapLayer, int normalMapLayer, int specularMapLayer, int index) {
		this.index = index;

		this.ambientColor = ambientColor;
		this.diffuseColor = diffuseColor;
		this.specularColor = specularColor;
		this.shininess = shininess;
		this.albedoMapIndex = albedoMapIndex;
		this.normalMapIndex = normalMapIndex;
		this.specularMapIndex = specularMapIndex;
		this.albedoMapLayer = albedoMapLayer;
		this.normalMapLayer = normalMapLayer;
		this.specularMapLayer = specularMapLayer;
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
		var albedoMapIndexBytes = ByteSerializer.toBytes(albedoMapIndex);
		var normalMapIndexBytes = ByteSerializer.toBytes(normalMapIndex);
		var specularMapIndexBytes = ByteSerializer.toBytes(specularMapIndex);
		var albedoMapLayerBytes = ByteSerializer.toBytes(albedoMapLayer);
		var normalMapLayerBytes = ByteSerializer.toBytes(normalMapLayer);
		var specularMapLayerBytes = ByteSerializer.toBytes(specularMapLayer);

		return ByteSerializer.squash(Arrays.asList(ambientBytes, albedoMapIndexBytes, diffuseBytes, normalMapIndexBytes,
				specularBytes, specularMapIndexBytes, shininessBytes, albedoMapLayerBytes, normalMapLayerBytes, specularMapLayerBytes));
	}

	/**
	 * Puts the bytes of this material into a buffer.
	 * 
	 * @param buffer the buffer to insert into
	 * @param index  the index to put the bytes at
	 */
	// TODO: Actually put all material data into the buffer
	public void putIntoBuffer(ByteBuffer buffer, int index) {
		for (var val : new float[] { ambientColor.x, ambientColor.y, ambientColor.z }) {
			buffer.putFloat(index, val);
			index += Float.BYTES;
		}
	}

	@Override
	public String toString() {
		return "Material[index=%d, ambientColor=%s]".formatted(index, ambientColor);
	}
}
