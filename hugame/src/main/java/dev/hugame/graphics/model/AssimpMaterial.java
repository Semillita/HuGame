package dev.hugame.graphics.model;

import java.util.Optional;

import org.joml.Vector3f;

import dev.hugame.graphics.Texture;

public record AssimpMaterial(
		Optional<Vector3f> albedoColor,
		Optional<Texture> albedoMap,
		Optional<Texture> normalMap,
		Optional<Texture> specularMap) {
}
