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
	private static int count;

	static {
		registry = new HashMap<>();
	}

	/**
	 * Returns a material based on the given creation parameters. If a material
	 * already exists which the given parameters, that material is returned,
	 * otherwise a new one is created and stored.
	 * 
	 * @param createInfo the parameters to use.
	 */
	public static Material get(MaterialCreateInfo createInfo) {
		if (registry.containsKey(createInfo)) {
			return registry.get(createInfo);
		} else {
			Material mat = new Material(createInfo, count++);
			registry.put(createInfo, mat);
			return mat;
		}
	}

	/** Returns a list of all materials created. */
	public static List<Material> collect() {
		return new ArrayList<>(registry.values());
	}

}
