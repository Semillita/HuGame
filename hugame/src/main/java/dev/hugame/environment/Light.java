package dev.hugame.environment;

import dev.hugame.graphics.Color;
import dev.hugame.util.Bufferable;

public abstract sealed class Light implements Bufferable permits DirectionalLight, PointLight, SpotLight {

	private Color color;

	public final Color getColor() {
		return color;
	}

}
