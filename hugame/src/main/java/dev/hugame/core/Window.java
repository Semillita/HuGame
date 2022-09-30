package dev.hugame.core;

import java.awt.Dimension;
import java.util.function.BiConsumer;

public interface Window {

	public void setVisible(boolean visible);
	
	public Dimension getSize();
	
	public int getWidth();
	
	public int getHeight();
	
	public int getX();
	
	public int getY();
	
	public void setResizeListener(BiConsumer<Integer, Integer> listener);
	
	public void requestAttention();
	
	public boolean close();
	
	public void destroy();
	
	public void pollEvents();
	
	public void clear(int r, int g, int b);
	
	public void swapBuffers();
	
	public boolean shouldClose();
	
	public void setShouldClose(boolean shouldClose);
}
