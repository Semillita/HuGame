package dev.hugame.desktop.gl;

import dev.hugame.core.Graphics;
import dev.hugame.core.GraphicsAPI;
import dev.hugame.core.Renderer;

public final class GLGraphics implements Graphics {

	private GLRenderer renderer;
	
	public GLGraphics() {
		renderer = new GLRenderer();
	}
	
	@Override
	public Renderer getRenderer() {
		return renderer;
	}

	@Override
	public GraphicsAPI getAPI() {
		return GraphicsAPI.OPENGL_DESKTOP;
	}

}
