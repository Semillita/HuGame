package dev.hugame.environment;

import java.util.ArrayList;
import java.util.List;

/** A description of a render environment with different light sources. */
public class Environment {

	private List<DirectionalLight> directionalLights;
	private List<PointLight> pointLights;
	private List<SpotLight> spotLights;

	public Environment() {
		directionalLights = new ArrayList<>();
		pointLights = new ArrayList<>();
		spotLights = new ArrayList<>();
	}

	/** Returns the directional lights in this environment. */
	public List<DirectionalLight> getDirectionalLights() {
		return directionalLights;
	}

	/** Returns the point lights in this environment. */
	public List<PointLight> getPointLights() {
		return pointLights;
	}

	/** Returns the spotlights in this environment */
	public List<SpotLight> getSpotLights() {
		return spotLights;
	}

	/** Adds a directional light to this environment. */
	public void add(DirectionalLight light) {
		directionalLights.add(light);
	}

	/** Adds a point light to this environment. */
	public void add(PointLight light) {
		pointLights.add(light);
	}

	/** Adds a spotlight to this environment. */
	public void add(SpotLight light) {
		spotLights.add(light);
	}

	/** Clears this envirnoment of any light sources. */
	public void clear() {
		List.of(directionalLights, pointLights, spotLights).forEach(List::clear);
	}

}
