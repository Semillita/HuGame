package dev.hugame.graphics.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3f;

/** Utility class to build and collect materials in a duplication-safe way. */
public class Materials {

	private static Map<MaterialCreateInfo, Material> registry;
	private static List<Material> materials;
	private static int count;

	static {
		registry = new HashMap<>();
		materials = new ArrayList<>();
	}

	/**
	 * Returns a material based on the given creation parameters. If a material
	 * already exists which the given parameters, that material is returned,
	 * otherwise a new one is created and stored.
	 * 
	 * @param createInfo the parameters to use.
	 */
	public static Material get(MaterialCreateInfo createInfo) {
		System.out.println("Creating material");
		var mat = new Material(createInfo, count++);
		materials.add(mat);
		return mat;
	}

	public static Material get(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float shininess,
			int albedoMapID, int normalMapID, int specularMapID, int albedoMapIndex, int normalMapIndex,
			int specularMapIndex) {
		var mat = new Material(ambientColor, diffuseColor, specularColor, shininess, albedoMapID, normalMapID,
				specularMapID, albedoMapIndex, normalMapIndex, specularMapIndex, count++);
		materials.add(mat);
		System.out.println("Creating material with diffuse " + diffuseColor + " at " + mat.getIndex());
		return mat;
	}

	/** Returns a list of all materials created. */
	public static List<Material> collect() {
		return materials;
	}

	/**
	 * Adds a material to the list of materials to be used by the renderer.
	 * 
	 * @return the ID of the material in the list
	 */
	public static int add(Material material) {
		materials.add(material);
		return materials.size() - 1;
	}

}
