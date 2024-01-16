package dev.hugame.graphics.material;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

/** Utility class to build and collect materials in a duplication-safe way. */
public class Materials {
	private static final List<Material> MATERIALS = new ArrayList<>();
	private static int count;

	public static Material get(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float shininess,
			int albedoMapIndex, int normalMapIndex, int specularMapIndex, int albedoMapLayer, int normalMapLayer,
			int specularMapLayer) {
		var mat = new Material(ambientColor, diffuseColor, specularColor, shininess, albedoMapIndex, normalMapIndex,
				specularMapIndex, albedoMapLayer, normalMapLayer, specularMapLayer, count++);
		MATERIALS.add(mat);
		return mat;
	}

	/** Returns a list of all materials created. */
	public static List<Material> collect() {
		return MATERIALS;
	}

	/**
	 * Adds a material to the list of materials to be used by the renderer.
	 * 
	 * @return the ID of the material in the list
	 */
	public static int add(Material material) {
		MATERIALS.add(material);
		return MATERIALS.size() - 1;
	}

}
