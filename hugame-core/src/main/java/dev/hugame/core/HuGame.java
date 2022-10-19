package dev.hugame.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.hugame.graphics.GLRenderer;
import dev.hugame.inject.InjectionEngine;
import dev.hugame.input.Input;
import dev.hugame.window.DesktopWindow;
import dev.hugame.window.WindowConfiguration;

public class HuGame {

	private static final boolean runningInJar;
	
	private static DesktopWindow window;
	private static Input input;
	private static GLRenderer renderer;
	private static ApplicationListener listener;
	private static InjectionEngine injectionEngine;

	static {
		runningInJar = HuGame.class
				.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.toString()
				.endsWith(".jar");
	}
	
	public static void start(ApplicationListener listener, WindowConfiguration config) {
		create(listener, config);
		mainloop();
		destroy();
	}
	
	public static void start(HuGameContext application) {
		
	}

	public static DesktopWindow getWindow() {
		return window;
	}

	public static Input getInput() {
		return input;
	}

	public static GLRenderer getRenderer() {
		return renderer;
	}
	
	public static boolean isRunningInJar() {
		return runningInJar;
	}

	public static void inject(Object object) {
		injectionEngine.injectIntoObject(object);
	}
	
	private static void create(ApplicationListener listener, WindowConfiguration config) {
		System.out.println("Creating...");
		
		window = new DesktopWindow(config);
		input = new Input(window.getHandle());
		GLFW.glfwMakeContextCurrent(window.getHandle());
		GL.createCapabilities();

		renderer = new GLRenderer();
		
		injectionEngine = new InjectionEngine();
		injectionEngine.setDefaultInstance(window);
		injectionEngine.setDefaultInstance(input);
		injectionEngine.setDefaultInstance(renderer);
		injectionEngine.start();
		
		HuGame.listener = listener;
		listener.onCreate();
		
		renderer.create();
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
