package io.semillita.hugame.core;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLUtil;

import io.semillita.hugame.annotations.WindowBuild;
import io.semillita.hugame.graphics.Renderer;
import io.semillita.hugame.input.Input;
import io.semillita.hugame.annotations.OnCreate;
import io.semillita.hugame.annotations.OnRender;
import io.semillita.hugame.annotations.OnClose;
import io.semillita.hugame.util.ClassFinder;
import io.semillita.hugame.window.Window;
import io.semillita.hugame.window.WindowConfiguration;

import static org.lwjgl.opengl.GL20.*;

public class HuGame {

	private static Window window;
	private static Input input;
	private static Renderer renderer;
	private static ApplicationListener listener;
	
	public static void start(ApplicationListener listener, WindowConfiguration config) {
		create(listener, config);
		mainloop();
		destroy();
	}
	
	public static Input getInput() {
		return input;
	}
	
	public static Renderer getRenderer() {
		return renderer;
	}
	
	private static void create(ApplicationListener listener, WindowConfiguration config) {
		System.out.println("Creating...");
		
		window = new Window(config);
		
		GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
		GLUtil.setupDebugMessageCallback(System.out);
		
		input = new Input(window.getHandle());
		GLFW.glfwMakeContextCurrent(window.getHandle());
		GL.createCapabilities();
		
		HuGame.listener = listener;
		listener.onCreate();
		
		renderer = new Renderer();
	}
	
	private static void mainloop() {
		System.out.println("Mainloop...");
		
		while (true) {
			if (window.shouldClose() && listener.onClose()) {
				break;
			} else {
//				System.out.println("-- Beginning of frame --");
				window.pollEvents();
				window.clear();
//				System.out.println("Rendering...");
				listener.onRender();
				window.swapBuffers();
			}
		}
	}
	
	private static void destroy() {
		System.out.println("Destroying...");
	}
	
}
