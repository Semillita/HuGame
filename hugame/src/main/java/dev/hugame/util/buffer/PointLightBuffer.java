package dev.hugame.util.buffer;

import java.util.List;

import dev.hugame.environment.Light;
import dev.hugame.environment.PointLight;
import dev.hugame.graphics.material.Material;

public class PointLightBuffer extends ShaderStorageBuffer<PointLight> {

	public static PointLightBuffer createFrom(List<PointLight> lights) {
		var handle = Buffer.generate();
		var buffer = new PointLightBuffer(handle, lights.size());
		buffer.fill(lights);
		return buffer;
	}
	
	public static PointLightBuffer allocate(int maxItems) {
		var handle = Buffer.generate();
		var buffer = new PointLightBuffer(handle, maxItems);
		return buffer;
	}

	private PointLightBuffer(int handle, int maxItems) {
		super(handle, PointLight.SIZE_IN_BYTES, maxItems);
	}

}
