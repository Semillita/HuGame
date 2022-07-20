package io.semillita.hugame.environment;

import io.semillita.hugame.graphics.Color;

public sealed class Light permits DirectionalLight, PointLight, SpotLight {

	private Color color;
	
	public final Color getColor() {
		return color;
	}
	
}
