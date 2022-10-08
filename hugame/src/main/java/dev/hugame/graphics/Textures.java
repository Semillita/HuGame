package dev.hugame.graphics;

import java.util.HashMap;
import java.util.Map;

import dev.hugame.util.Files;

/** Utility class for loading and caching texture file content. */
public class Textures {

	private static Map<String, Texture> loadedTextures;
	
	static {
		loadedTextures = new HashMap<>();
	}
	
	public static Texture get(String filepath) {
		if (loadedTextures.containsKey(filepath)) {
			return loadedTextures.get(filepath);
		} else {
			var maybeSource = Files.read(filepath);
			if (maybeSource.isEmpty()) return null;
			var texture = new Texture(maybeSource.get());
			loadedTextures.put(filepath, texture);
			return texture;
		}
	}
	
}
