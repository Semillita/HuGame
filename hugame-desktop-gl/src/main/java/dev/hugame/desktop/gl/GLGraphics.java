package dev.hugame.desktop.gl;

import dev.hugame.core.Graphics;
import dev.hugame.core.GraphicsAPI;
import dev.hugame.core.Renderer;
import dev.hugame.graphics.Texture;
import dev.hugame.util.ImageLoader;

public final class GLGraphics implements Graphics {

	private final GLRenderer renderer;
	private TextureCollector textureCollector;
	
	public GLGraphics() {
		renderer = new GLRenderer();
		textureCollector = new TextureCollector();
	}
	
	@Override
	public Renderer getRenderer() {
		return renderer;
	}

	@Override
	public GraphicsAPI getAPI() {
		return GraphicsAPI.OPENGL_DESKTOP;
	}

	@Override
	public Texture getTexture(byte[] bytes) {
		var imageData = ImageLoader.read(bytes, 4);
		var texture = textureCollector.addTexture(imageData);
		
		return texture;
	}

	@Override
	public void create() {
		textureCollector.generate();
	}
	
}
