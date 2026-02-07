package dev.hugame.desktop.gl;

import dev.hugame.desktop.gl.buffer.*;
import dev.hugame.desktop.gl.renderer.ModelRenderer;
import dev.hugame.desktop.gl.renderer.QuadRenderer;
import dev.hugame.graphics.RenderTarget;
import dev.hugame.graphics.text.Font;
import org.joml.Vector3f;

import dev.hugame.graphics.Renderer;
import dev.hugame.environment.Environment;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.util.Transform;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL43.*;

/** Renderer master class for rendering all different components */
public class GLRenderer implements Renderer {
	private final GLGraphics graphics;
	private final PerspectiveCamera camera;

    private final ModelRenderer modelRenderer;
    private final QuadRenderer quadRenderer;

	private boolean initialized = false;
	private RenderTarget renderTarget;
	
	public GLRenderer(GLGraphics graphics) {
		this.graphics = graphics;

		camera = new PerspectiveCamera(new Vector3f(200f, 200f, 200f));
		camera.lookAt(new Vector3f(0, 0, 0));
		camera.update();

        this.modelRenderer = new ModelRenderer(this);
        this.quadRenderer = new QuadRenderer(this);

		// TODO: Extract into separate class for convenience
		glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
			var buffer = MemoryUtil.memByteBuffer(message, length);
			var messageString = MemoryUtil.memUTF8(buffer);

			System.out.println("OpenGL error: " + messageString);
		}, 0);
		glEnable(GL_DEBUG_OUTPUT);

		glEnable(GL_BLEND);
	}
	
	@Override
	public void create() {
		if (initialized) {
			return;	
		}
		
		initialized = true;
		
		var materials = Materials.collect();
		modelRenderer.setMaterials(materials);
	}

	@Override
	public void beginFrame() {
		var clearColor = graphics.getClearColor();
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void endFrame() {
		// No need to do anything
	}

	@Override
	@Deprecated
	public void draw(Model model, Transform transform, /*Unused*/Material material) {
		draw(model, transform);
	}
	
	// We should actually support drawing a model with some random material, like wood
	@Override
	public void draw(Model model, Transform transform) {
		modelRenderer.draw(model, transform);
	}

	@Override
	public void drawText(String text, Font font, int fontSize, int x, int y) {

	}

	@Override
	public void flushTextRenderer() {

	}

	@Override
	public void flush() {
		modelRenderer.flush();
	}

	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public void updateEnvironment(Environment environment) {
		modelRenderer.updateEnvironment(environment);
	}

	@Override
	public void setRenderTarget(RenderTarget renderTarget) {
		this.renderTarget = renderTarget;
	}

	public void renderBatch(GLBatch batch) {
		quadRenderer.renderBatch(batch);
	}
}
