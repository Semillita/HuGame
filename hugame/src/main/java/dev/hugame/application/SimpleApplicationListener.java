package dev.hugame.application;

import dev.hugame.core.ApplicationListener;

public interface SimpleApplicationListener {
	/**
	 * Called when the application is being created.
	 *
	 * Application initialization code should be put here and not in the constructor
	 * of a class implementing this interface.
	 */
	void onCreate(HuGameApplicationContext applicationContext);

	/** Called on every frame, locked to V-sync. */
	void onRender();

	/**
	 * Called when an attempt to close the application window has been made.
	 *
	 * @return whether the window close event should go through or not.
	 */
	default boolean shouldClose() {
		return true;
	}

	/**
	 * Called when the application is being destroyed.
	 * */
	void onDestroy();
}
