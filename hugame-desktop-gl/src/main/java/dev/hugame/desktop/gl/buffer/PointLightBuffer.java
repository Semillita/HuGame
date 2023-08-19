package dev.hugame.desktop.gl.buffer;

import java.util.List;

import dev.hugame.environment.Light;
import dev.hugame.environment.PointLight;
import dev.hugame.graphics.material.Material;

public class PointLightBuffer extends ShaderStorageBuffer<PointLight> {

	public static PointLightBuffer createFrom(List<PointLight> lights) {
		var handle = Buffer.generate();
		var buffer = new PointLightBuffer(handle);
		buffer.fill(lights);
		return buffer;
	}
	
	public static PointLightBuffer allocateNew(int maxItems) {
		var handle = Buffer.generate();
		var buffer = new PointLightBuffer(handle);
		buffer.allocate(maxItems);
		return buffer;
	}

	private PointLightBuffer(int handle) {
		super(handle, PointLight.SIZE_IN_BYTES);
	}

}
