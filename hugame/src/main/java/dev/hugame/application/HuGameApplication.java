package dev.hugame.application;

public abstract class HuGameApplication {

	private boolean running = false;
	// TODO: Implement pausing
	private boolean paused = false;
	private boolean finished = false;

	public final void start() {
		if (running) {
			throw new RuntimeException("[HuGame] Failed to start application: application already running");
		}

		if (finished) {
			throw new RuntimeException("[HuGame] Failed to start application: application finished");
		}

		run();
	}

	protected void run() {
		create();
		loop();
		destroy();
	}

	protected abstract void create();

	protected void loop() {
		while (!shouldClose()) {
			update();
		}
	}

	protected abstract boolean shouldClose();

	protected abstract void update();

	protected abstract void destroy();
}
