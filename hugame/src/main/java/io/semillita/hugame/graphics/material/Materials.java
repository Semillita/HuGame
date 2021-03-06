package io.semillita.hugame.graphics.material;

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
		registry = new HashMap();
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
		System.out.println("Collected materials");
		registry.values().forEach(mat -> System.out.println(mat));
			return new ArrayList<Material>(registry.values());
		}
	
	}
