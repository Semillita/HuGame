package io.semillita.hugame.graphics;

import io.semillita.hugame.graphics.material.Material;
import io.semillita.hugame.util.Transform;

public record InstanceData(Transform transform, Material material) {
}
