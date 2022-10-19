package dev.hugame.graphics;

import java.util.HashMap;
import java.util.Map;

import dev.hugame.core.Graphics;
import dev.hugame.core.HuGame;
import dev.hugame.inject.Inject;
import dev.hugame.util.Files;

/** Utility class for loading and caching texture file content. 
 *  Might become deprecated, or at least have its static API deprecated
 *  in favor of a dynamic, ideally injected, one.*/
public class Textures {

	private static Map<String, Texture> loadedTextures;
	
	private static @Inject Graphics graphics;
	
	static {
		loadedTextures = new HashMap<>();
		HuGame.injectStatic(Textures.class);
	}
	
	public static Texture get(String filepath) {
		if (loadedTextures.containsKey(filepath)) {
			return loadedTextures.get(filepath);
		} else {
			var maybeSource = Files.readBytes(filepath);
			if (maybeSource.isEmpty()) return null;
			var texture = graphics.getTexture(maybeSource.get());
			loadedTextures.put(filepath, texture);
			return texture;
		}
	}
	
}
