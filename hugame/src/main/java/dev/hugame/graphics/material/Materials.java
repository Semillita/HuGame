package dev.hugame.graphics.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3f;

public class Materials {

	private static Map<MaterialCreateInfo, Material> registry;
	private static int count;

	static {
		registry = new HashMap<>();
	}

	public static Material get(MaterialCreateInfo createInfo) {
		if (registry.containsKey(createInfo)) {
			return registry.get(createInfo);
		} else {
			Material mat = new Material(createInfo, count++);
			registry.put(createInfo, mat);
			return mat;
		}
	}

	public static List<Material> collect() {
		return new ArrayList<>(registry.values());
	}

}
