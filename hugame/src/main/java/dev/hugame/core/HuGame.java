package dev.hugame.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.hugame.graphics.Textures;
import dev.hugame.inject.Inject;
import dev.hugame.inject.InjectionEngine;

public class HuGame {
	// TODO: Add some System class with utils like injection and jar state check.

	private static final boolean runningInJar;

	private static Graphics graphics;
	private static Window window;
	private static Input input;
	private static Renderer renderer;
	private static ApplicationListener listener;
	private static InjectionEngine injectionEngine;

	static {
		runningInJar = HuGame.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar");
	}

	/**
	 * Start the engine here.
	 * 
	 * @param context  a context specific to the platform and API
	 * @param listener the listener controlling the application
	 */
	public static void start(HuGameContext context, ApplicationListener listener) {
		create(context, listener);
		mainloop();
		destroy();
	}

	public static Graphics getGraphics() {
		return graphics;
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

	/** Returns whether the entire application is running in a compiled JAR file. */
	public static boolean isRunningInJar() {
		return runningInJar;
	}

	/** Sends an object to have its fields annotated with {@link Inject} injected. */
	public static void inject(Object object) {
		injectionEngine.injectIntoObject(object);
	}
	
	public static void injectStatic(Class<?> c) {
		injectionEngine.injectIntoClass(c);
	}

	/**
	 * Creates a HuGame application
	 * 
	 * @param context  the application context to be used
	 * @param listener the application listener to be used
	 */
	private static void create(HuGameContext context, ApplicationListener listener) {
		System.out.println("Creating...");
		HuGame.listener = listener;
		var graphics = context.getGraphics();
		HuGame.graphics = graphics;
		HuGame.renderer = graphics.getRenderer();
		HuGame.window = context.getWindow();
		HuGame.input = context.getInput();

		injectionEngine = new InjectionEngine();
		injectionEngine.setDefaultInstance(graphics, Graphics.class);
		injectionEngine.setDefaultInstance(window, Window.class);
		injectionEngine.setDefaultInstance(input, Input.class);
		injectionEngine.setDefaultInstance(renderer, Renderer.class);
		injectionEngine.start();

		listener.onCreate();
		
		graphics.create();
		
		renderer.create();
	}

	/** Initiates the main loop of the application */
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
				window.clear(1f, 1f, 0.3f);
				listener.onRender();
				window.swapBuffers();
			}
		}
	}

	/** Destroys the application */
	private static void destroy() {
		System.out.println("Destroying...");

		window.destroy();
	}

}
