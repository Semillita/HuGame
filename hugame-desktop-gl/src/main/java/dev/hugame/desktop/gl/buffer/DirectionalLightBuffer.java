package dev.hugame.desktop.gl.buffer;

import java.util.List;

import dev.hugame.environment.DirectionalLight;

public class DirectionalLightBuffer extends ShaderStorageBuffer<DirectionalLight> {

	public static DirectionalLightBuffer createFrom(List<DirectionalLight> lights) {
		var handle = Buffer.generate();
		var buffer = new DirectionalLightBuffer(handle, lights.size());
		buffer.fill(lights);
		return buffer;
	}
	
	public static DirectionalLightBuffer allocate(int maxItems) {
		var handle = Buffer.generate();
		var buffer = new DirectionalLightBuffer(handle, maxItems);
		return buffer;
	}

	private DirectionalLightBuffer(int handle, int maxItems) {
		super(handle, DirectionalLight.SIZE_IN_BYTES, maxItems);
	}
	
}
