package io.semillita.hugame.environment;

import io.semillita.hugame.graphics.Color;
import io.semillita.hugame.util.Bufferable;

public abstract sealed class Light implements Bufferable permits DirectionalLight, PointLight, SpotLight {

	private Color color;

	public final Color getColor() {
		return color;
	}

}
