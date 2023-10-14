package dev.hugame.util;

import dev.hugame.core.Graphics;
import dev.hugame.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class TextureLoader {
	private final Graphics graphics;
	private final Map<String, Texture> loadedTextures;

	public TextureLoader(Graphics graphics) {
		this.graphics = graphics;
		this.loadedTextures = new HashMap<>();
	}

	public Texture get(String filepath) {
		if (loadedTextures.containsKey(filepath)) {
			return loadedTextures.get(filepath);
		} else {
			var maybeSource = Files.readBytes(filepath);
			if (maybeSource.isEmpty()) return null;
			var texture = graphics.createTexture(maybeSource.get());
			loadedTextures.put(filepath, texture);
			return texture;
		}
	}
}
