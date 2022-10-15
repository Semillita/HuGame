package dev.hugame.graphics.material;

import org.joml.Vector3f;

/** Information about a material, used to avoid material copies. */
public record MaterialCreateInfo(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor,
		float shininess) {
}
