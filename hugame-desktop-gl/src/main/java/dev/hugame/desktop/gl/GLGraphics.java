package dev.hugame.desktop.gl;

import dev.hugame.core.Graphics;
import dev.hugame.core.GraphicsAPI;
import dev.hugame.core.Renderer;
import dev.hugame.desktop.gl.model.OpenGLModel;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.model.Model;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.ImageLoader;
import dev.hugame.window.DesktopWindow;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL45.*;

public final class GLGraphics implements Graphics {

	private final GLRenderer renderer;
	private final DesktopWindow window;
	private TextureCollector textureCollector;

	public GLGraphics(DesktopWindow window) {
		GL.createCapabilities();

		renderer = new GLRenderer();
		this.window = window;
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
	public Texture createTexture(byte[] bytes) {
		var imageData = ImageLoader.read(bytes, 4);

		return textureCollector.addTexture(imageData);
	}

	@Override
	public Batch createBatch() {
		return new GLBatch(renderer);
	}

	@Override
	public Model createModel(ResolvedModel resolvedModel) {
		return OpenGLModel.from(resolvedModel);
	}

	@Override
	public void create() {
		textureCollector.generate();
	}

	@Override
	public void swapBuffers() {
		window.swapBuffers();
	}

	@Override
	public void clear(float red, float green, float blue) {
		glClearColor(red, green, blue, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
}
