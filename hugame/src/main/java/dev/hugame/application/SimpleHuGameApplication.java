package dev.hugame.application;

import dev.hugame.core.Graphics;
import dev.hugame.core.Input;
import dev.hugame.core.Window;
import dev.hugame.model.spec.DelegatingModelLoader;
import dev.hugame.model.spec.ModelLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SimpleHuGameApplication extends HuGameApplication {
	private final SimpleApplicationListener listener;
	private final Supplier<SimpleApplicationConfiguration> makeConfiguration;

	private Graphics graphics;
	private Window window;

	protected HuGameApplicationContext applicationContext;

	public SimpleHuGameApplication(SimpleApplicationListener applicationListener, Supplier<SimpleApplicationConfiguration> makeConfiguration) {
		this.listener = applicationListener;
		this.makeConfiguration = makeConfiguration;
	}

	@Override
	protected void create() {
		var configuration = makeConfiguration.get();
		if (configuration == null) {
			throw new RuntimeException("[HuGame] Failed to create application properties: configuration is null");
		}

		this.window = configuration.window();
		this.graphics = configuration.graphics();
		var input = configuration.input();
		var modelLoaders = configuration.modelLoaders();

		this.applicationContext = makeContext(window, graphics, input, modelLoaders);

		listener.onCreate(applicationContext);
		graphics.create();
		graphics.getRenderer().create();
	}

	@Override
	protected boolean shouldClose() {
		if (window.shouldClose()) {
			System.out.println("Window should close");
			if (listener.shouldClose()) {
				return true;
			} else {
				window.setShouldClose(false);
			}
		}

		return false;
	}

	@Override
	protected void update() {
		window.pollEvents();
		// TODO: Don't clear like this, instead set graphics clearColor value
		graphics.getRenderer().beginFrame();
		//graphics.clear(1f, 1f, 0.5f, 0.0f);
		graphics.clear(0f, 0f, 1, 1);
		listener.onRender();
		graphics.getRenderer().endFrame();
		graphics.swapBuffers();
	}

	@Override
	protected void destroy() {
		listener.onDestroy();
	}

	private HuGameApplicationContext makeContext(Window window, Graphics graphics, Input input, List<ModelLoader> modelLoaders) {
		return new HuGameApplicationContext() {

			@Override
			public Window getWindow() {
				return window;
			}

			@Override
			public Graphics getGraphics() {
				return graphics;
			}

			@Override
			public Input getInput() {
				return input;
			}

			@Override
			public ModelLoader getModelLoader() {
				return new DelegatingModelLoader(new ArrayList<>(modelLoaders));
			}
		};
	}
}
