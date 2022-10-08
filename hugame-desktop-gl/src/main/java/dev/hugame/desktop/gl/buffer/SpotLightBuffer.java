package dev.hugame.desktop.gl.buffer;

import java.util.List;

import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;

public class SpotLightBuffer extends ShaderStorageBuffer<SpotLight> {

	public static SpotLightBuffer createFrom(List<SpotLight> lights) {
		var handle = Buffer.generate();
		var buffer = new SpotLightBuffer(handle, lights.size());
		buffer.fill(lights);
		return buffer;
	}
	
	public static SpotLightBuffer allocate(int maxItems) {
		var handle = Buffer.generate();
		var buffer = new SpotLightBuffer(handle, maxItems);
		return buffer;
	}

	private SpotLightBuffer(int handle, int maxItems) {
		super(handle, SpotLight.SIZE_IN_BYTES, maxItems);
	}
	
}
