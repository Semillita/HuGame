package io.semillita.hugame.environment;

import java.util.ArrayList;
import java.util.List;

public class Environment {

	private List<DirectionalLight> directionalLights;
	private List<PointLight> pointLights;
	private List<SpotLight> spotLights;
	
	public Environment() {
		directionalLights = new ArrayList<>();
		pointLights = new ArrayList<>();
		spotLights = new ArrayList<>();
	}
	
	public List<DirectionalLight> getDirectionalLights() {
		return directionalLights;
	}
	
	public List<PointLight> getPointLights() {
		return pointLights;
	}
	
	public List<SpotLight> getSpotLights() {
		return spotLights;
	}
	
	public void add(DirectionalLight light) {
		directionalLights.add(light);
	}

	public void add(PointLight light) {
		pointLights.add(light);
	}
	
	public void add(SpotLight light) {
		spotLights.add(light);
	}

	public void clear() {
		List.of(directionalLights, pointLights, spotLights).forEach(List::clear);
	}
	
}
