package dev.hugame.model.spec;

import dev.hugame.graphics.Texture;
import org.joml.Vector3f;

import java.util.Optional;

public class ResolvedMaterial {
	private final Optional<Vector3f> albedoColor;

	private final Optional<Texture> albedoMap;

	private final Optional<Texture> normalMap;

	private final Optional<Texture> specularMap;

	public ResolvedMaterial(Optional<Vector3f> albedoColor, Optional<Texture> albedoMap, Optional<Texture> normalMap, Optional<Texture> specularMap) {
		this.albedoColor = albedoColor;
		this.albedoMap = albedoMap;
		this.normalMap = normalMap;
		this.specularMap = specularMap;}

	public Optional<Vector3f> getAlbedoColor() {
		return albedoColor;
	}

	public Optional<Texture> getAlbedoMap() {
		return albedoMap;
	}

	public Optional<Texture> getNormalMap() {
		return normalMap;
	}

	public Optional<Texture> getSpecularMap() {
		return specularMap;
	}
}
