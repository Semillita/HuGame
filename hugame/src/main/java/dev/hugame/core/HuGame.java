package dev.hugame.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.hugame.inject.InjectionEngine;

public class HuGame {

	private static final boolean runningInJar;
	
	private static Window window;
	private static Input input;
	private static Renderer renderer;
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
	
	public static void start(HuGameContext context, ApplicationListener listener) {
		create(context, listener);
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
	
	public static boolean isRunningInJar() {
		return runningInJar;
	}

	public static void inject(Object object) {
		injectionEngine.injectIntoObject(object);
	}
	
	private static void create(HuGameContext context, ApplicationListener listener) {
		System.out.println("Creating...");
		HuGame.listener = listener;
		var graphics = context.getGraphics();
		HuGame.renderer = graphics.getRenderer();
		HuGame.window = context.getWindow();
		HuGame.input = context.getInput();
		
		injectionEngine = new InjectionEngine();
		injectionEngine.setDefaultInstance(window, Window.class);
		injectionEngine.setDefaultInstance(input, Input.class);
		injectionEngine.setDefaultInstance(renderer, Renderer.class);
		injectionEngine.start();
		
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
				window.clear(1, 1, 1);
				listener.onRender();
				window.swapBuffers();
			}
		}
	}

	private static void destroy() {
		System.out.println("Destroying...");
		
		window.destroy();
	}

}
