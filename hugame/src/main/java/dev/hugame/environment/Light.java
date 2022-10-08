package dev.hugame.environment;

import dev.hugame.graphics.Color;
import dev.hugame.util.Bufferable;

/** Model to describe a generic light source. */
public abstract sealed class Light implements Bufferable permits DirectionalLight, PointLight, SpotLight {

	private Color color;

	/** Returns the color of this light */
	public final Color getColor() {
		return color;
	}

}
