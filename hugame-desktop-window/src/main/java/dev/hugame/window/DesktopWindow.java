package dev.hugame.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;

import java.awt.Dimension;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL;

import dev.hugame.core.Window;

public class DesktopWindow implements Window {

	static {
		glfwInit();
	}
	
	private final long handle;
	private Runnable onRender;
	private Supplier<Boolean> onClose;
	private Optional<BiConsumer<Integer, Integer>> maybeResizeListener;
	private Dimension size;
	
	public DesktopWindow(WindowConfiguration config) {
		glfwWindowHint(GLFW_RESIZABLE, config.resizable ? 1 : 0);
		glfwWindowHint(GLFW_DECORATED, config.decorated ? 1 : 0);
		glfwWindowHint(GLFW_FOCUSED, config.focused ? 1 : 0);
		glfwWindowHint(GLFW_AUTO_ICONIFY, config.autoIconify ? 1 : 0);
		glfwWindowHint(GLFW_FLOATING, config.floating ? 1 : 0);
		glfwWindowHint(GLFW_MAXIMIZED, config.maximized ? 1 : 0);
		glfwWindowHint(GLFW_CENTER_CURSOR, config.centerCursor ? 1 : 0);
		glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, config.transparentFramebuffer ? 1 : 0);
		glfwWindowHint(GLFW_FOCUS_ON_SHOW, config.focusOnShow ? 1 : 0);
		glfwWindowHint(GLFW_SAMPLES, 8);
		
		long monitor = (config.fullscreen) ? glfwGetPrimaryMonitor() : 0;
		
		handle = glfwCreateWindow(config.width, config.height, config.title, monitor, 0);
		
		glfwSetWindowPos(handle, config.x, config.y);
				
		glfwMakeContextCurrent(handle);
		glfwSwapInterval(GLFW_TRUE);
		
		glfwShowWindow(handle);
		
		GL.createCapabilities();
		
		size = new Dimension(config.width, config.height);
		
		maybeResizeListener = Optional.empty();
		glfwSetWindowSizeCallback(handle, this::resizeCallback);
		glfwMakeContextCurrent(handle);
	}
	
	@Override
	public void setVisible(boolean visible) {
	}

	@Override
	public Dimension getSize() {
		return size;
	}
	
	@Override
	public int getWidth() {
		return size.width;
	}
	
	@Override
	public int getHeight() {
		return size.height;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void setResizeListener(BiConsumer<Integer, Integer> resizeListener) {
		maybeResizeListener = Optional.ofNullable(resizeListener);
	}
	
	@Override
	public void requestAttention() {
		glfwRequestWindowAttention(handle);
	}

	@Override
	public boolean close() {
		return (onClose != null) ? onClose.get() : true;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	public long getHandle() {
		return handle;
	}
	
	@Override
	public void pollEvents() {
		glfwPollEvents();
	}
	
	@Override
	public void clear(float r, float g, float b) {
		glClearColor(r, g, b, 1);	
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	@Override
	public void swapBuffers() {
		glfwSwapBuffers(handle);
	}
	
	@Override
	public boolean shouldClose() {
		return glfwWindowShouldClose(handle);
	}
	
	@Override
	public void setShouldClose(boolean shouldClose) {
		glfwSetWindowShouldClose(handle, shouldClose);
	}
	
	private void resizeCallback(long handle, int width, int height) {
		size = new Dimension(width, height);
		glViewport(0, 0, width, height);
		maybeResizeListener.ifPresent(resizeListener -> resizeListener.accept(width, height));
	}
	
}
