package io.semillita.hugame.util.buffer;

import java.util.List;

import io.semillita.hugame.environment.Light;
import io.semillita.hugame.environment.PointLight;
import io.semillita.hugame.graphics.MaterialBuffer;
import io.semillita.hugame.graphics.material.Material;

public class PointLightBuffer extends ShaderStorageBuffer<PointLight> {

	public static PointLightBuffer createFrom(List<PointLight> lights) {
		var handle = Buffer.newBuffer();
		var buffer = new PointLightBuffer(handle);
		buffer.fill(lights);
		return buffer;
	}

	private PointLightBuffer(int handle) {
		super(handle, PointLight.SIZE_IN_BYTES);
	}

}
