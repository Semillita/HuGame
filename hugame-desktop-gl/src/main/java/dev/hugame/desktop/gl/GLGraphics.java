package dev.hugame.desktop.gl;

import dev.hugame.core.Graphics;
import dev.hugame.core.GraphicsAPI;
import dev.hugame.core.Renderer;
import dev.hugame.desktop.gl.model.OpenGLModel;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.ResolvedTexture;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.model.Model;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.util.ImageLoader;
import dev.hugame.window.DesktopWindow;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL45.*;

public final class GLGraphics implements Graphics {

	private final GLRenderer renderer;
	private final DesktopWindow window;
	private TextureCollector textureCollector;
	private Vector4f clearColor;

	public GLGraphics(DesktopWindow window) {
		GL.createCapabilities();

		renderer = new GLRenderer(this);
		this.window = window;
		textureCollector = new TextureCollector();

		clearColor = new Vector4f(0, 0, 0, 1);
	}
	
	@Override
	public Renderer getRenderer() {
		return renderer;
	}

	@Override
	public GraphicsAPI getAPI() {
		return GraphicsAPI.OPENGL;
	}

	@Override
	public Texture createTexture(ResolvedTexture resolvedTexture) {
		return textureCollector.addTexture(resolvedTexture);
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

	// TODO: Should not be done anytime, rather set the color through getClearColor and clear at another time
	@Override
	public void clear(float red, float green, float blue, float alpha) {
		//glClearColor(red, green, blue, alpha);
		//glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void setClearColor(float red, float green, float blue, float alpha) {
		clearColor = new Vector4f(red, green, blue, alpha);
	}

	public Vector4f getClearColor() {
		return clearColor;
	}

}
