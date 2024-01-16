package dev.hugame.desktop.gl.buffer;

import java.util.List;

import dev.hugame.environment.SpotLight;

public class SpotLightBuffer extends GLShaderStorageBuffer<SpotLight> {

	public static SpotLightBuffer createFrom(List<SpotLight> lights) {
		var handle = Buffer.generate();
		var buffer = new SpotLightBuffer(handle);
		buffer.fill(lights);
		return buffer;
	}
	
	public static SpotLightBuffer allocateNew(int maxItems) {
		var handle = Buffer.generate();
		var buffer = new SpotLightBuffer(handle);
		buffer.allocate(maxItems);
		return buffer;
	}

	private SpotLightBuffer(int handle) {
		super(handle, SpotLight.SIZE_IN_BYTES);
	}
	
}
