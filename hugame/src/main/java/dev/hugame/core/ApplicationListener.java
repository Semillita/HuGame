package dev.hugame.core;

/** Listener responsible for general application events. */
public class ApplicationListener {

	/**
	 * Called when the application is being created.
	 * 
	 * Application initialization code should be put here and not in the constructor
	 * of a class implementing {@link ApplicationListener}.
	 */

	public void onCreate() {

	}

	/** Called on every frame, locked to V-sync. */
	public void onRender() {

	}

	/**
	 * Called when an attempt to close the application window has been made.
	 * 
	 * @return whether the window close event should go through or not.
	 */
	public boolean onClose() {
		return true;
	}

}
