package dev.hugame.graphics.material;

import org.joml.Vector4f;

/** Information about a material, used to avoid material copies. */
public record MaterialCreateInfo(Vector4f ambientColor) {
}
