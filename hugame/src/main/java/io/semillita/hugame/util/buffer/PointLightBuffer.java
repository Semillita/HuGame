package io.semillita.hugame.util.buffer;

import java.util.List;

import io.semillita.hugame.environment.Light;
import io.semillita.hugame.environment.PointLight;
import io.semillita.hugame.graphics.material.Material;

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
