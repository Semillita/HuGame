package dev.hugame.graphics;

public interface Batch {

	/** Prepares this batch for accepting draw calls. */
	// TODO: Make this happen automatically if drawn to when closed.
	public void begin();
	
	/**
	 * Flushes this batch to the renderer.
	 * 
	 * @see GLBatch#flush()
	 */
	public void end();
	
	/**
	 * Adds a texture to this batch's draw queue.
	 * 
	 * @param texture the texutre to use
	 * @param x       the x-coordinate of the bottom-left corner
	 * @param y       the y-coordinate of the bottom-left corner
	 * @param width   the distance between left and right edge
	 * @param height  the distance between bottom and top edge
	 */
	public void draw(Texture texture, int x, int y, int width, int height);
	
	/** Sets the camera to be used to draw this batch. */
	public void setCamera(Camera2D camera);

	/** Returns the camera used to draw this batch. */
	Camera2D getCamera();

	/** Flushes this batch to the renderer. */
	public void flush();
}
