package dev.hugame.desktop.gl.buffer;

import java.util.List;

import dev.hugame.graphics.material.Material;

public class MaterialBuffer extends GLShaderStorageBuffer<Material> {

	public static MaterialBuffer createFrom(List<Material> materials) {
		var handle = Buffer.generate();
		var buffer = new MaterialBuffer(handle, materials.size());
		buffer.fill(materials);
		return buffer;
	}

	private MaterialBuffer(int handle, int maxItems) {
		super(handle, Material.SIZE_IN_BYTES);
	}

}
