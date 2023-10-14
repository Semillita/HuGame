package dev.hugame.window;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Dimension;
import java.util.Optional;
import java.util.function.BiConsumer;

import dev.hugame.core.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

public class DesktopWindow implements Window {

	static {
		glfwInit();
	}
	
	private final long handle;
	private final BiConsumer<Integer, Integer> updateViewport;
	private Optional<BiConsumer<Integer, Integer>> maybeResizeListener;
	private Dimension size;
	
	public DesktopWindow(WindowConfiguration config, BiConsumer<Integer, Integer> updateViewport) {
		glfwSetErrorCallback((error, descriptionPointer) -> {
			var message = MemoryUtil.memUTF8(descriptionPointer);
			System.err.println("GLFW Error " + error + ": " + message);
		});

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

		size = new Dimension(config.width, config.height);
		
		maybeResizeListener = Optional.empty();
		glfwSetWindowSizeCallback(handle, this::resizeCallback);
		glfwMakeContextCurrent(handle);

		this.updateViewport = updateViewport;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			glfwShowWindow(handle);
		} else {
			glfwHideWindow(handle);
		}
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
		var xPosBuffer = BufferUtils.createIntBuffer(1);

		glfwGetWindowPos(handle, xPosBuffer, BufferUtils.createIntBuffer(1));

		return xPosBuffer.get(0);
	}

	@Override
	public int getY() {
		var yPosBuffer = BufferUtils.createIntBuffer(1);

		glfwGetWindowPos(handle, BufferUtils.createIntBuffer(1), yPosBuffer);

		return yPosBuffer.get(0);
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
	public void destroy() {
		glfwDestroyWindow(handle);
	}
	
	public long getHandle() {
		return handle;
	}
	
	@Override
	public void pollEvents() {
		glfwPollEvents();
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
		updateViewport.accept(width, height);
		maybeResizeListener.ifPresent(resizeListener -> resizeListener.accept(width, height));
	}
	
}
