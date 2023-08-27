package dev.hugame.desktop.gl;

import org.lwjgl.opengl.GL;

import dev.hugame.core.Graphics;
import dev.hugame.core.HuGameContext;
import dev.hugame.core.Input;
import dev.hugame.core.Window;
import dev.hugame.window.DesktopInput;
import dev.hugame.window.DesktopWindow;
import dev.hugame.window.WindowConfiguration;

import static org.lwjgl.opengl.GL45.*;

public final class DesktopGLContext implements HuGameContext {

	private GLGraphics graphics;
	private DesktopWindow window;
	private DesktopInput input;

	public DesktopGLContext(WindowConfiguration windowConfig) {
		window = new DesktopWindow(windowConfig, () -> GL.createCapabilities(), (width, height) -> glViewport(0, 0, width, height));
		GL.createCapabilities();
		graphics = new GLGraphics();
		input = new DesktopInput(window.getHandle());
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	@Override
	public Window getWindow() {
		return window;
	}
	
	@Override
	public Input getInput() {
		return input;
	}

}
