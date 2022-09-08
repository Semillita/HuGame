package dev.hugame.core;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLUtil;

import dev.hugame.graphics.Renderer;
import dev.hugame.inject.InjectionEngine;
import dev.hugame.input.Input;
import dev.hugame.util.ClassFinder;
import dev.hugame.window.Window;
import dev.hugame.window.WindowConfiguration;

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

	public static Window getWindow() {
		return window;
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
		input = new Input(window.getHandle());
		GLFW.glfwMakeContextCurrent(window.getHandle());
		GL.createCapabilities();

		HuGame.listener = listener;
		listener.onCreate();

		// Dependency injection
		InjectionEngine inj = new InjectionEngine();
		inj.start();
		//
		
		renderer = new Renderer();
	}

	private static void mainloop() {
		System.out.println("Mainloop...");

		while (true) {
			if (window.shouldClose()) {
				if (listener.onClose()) {
					break;
				} else {
					window.setShouldClose(false);
				}
			} else {
				window.pollEvents();
				window.clear();
				listener.onRender();
				window.swapBuffers();
			}
		}
	}

	private static void destroy() {
		System.out.println("Destroying...");
	}

}
