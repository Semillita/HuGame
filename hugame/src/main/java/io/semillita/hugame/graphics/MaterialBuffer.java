package io.semillita.hugame.graphics;

import io.semillita.hugame.graphics.material.Material;
import io.semillita.hugame.util.buffer.Buffer;
import io.semillita.hugame.util.buffer.ShaderStorageBuffer;

import java.util.List;

public class MaterialBuffer extends ShaderStorageBuffer<Material> {

	public static MaterialBuffer createFrom(List<Material> materials) {
		var handle = Buffer.newBuffer();
		var buffer = new MaterialBuffer(handle);
		buffer.fill(materials);
		return buffer;
	}

	private MaterialBuffer(int handle) {
		super(handle, Material.SIZE_IN_BYTES);
	}

}
