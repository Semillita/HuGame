package dev.hugame.window;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import dev.hugame.core.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.system.MemoryStack.stackPush;

// TODO: Make this not extend Window, instead let the engine create a
//       Window impl forwarding calls to a DesktopWindow (or mobile)
public class DesktopWindow implements Window {

	static {
		glfwInit();
	}
	
	private final long handle;
	private final BiConsumer<Integer, Integer> updateViewport; // TODO: Remove, can't really have final
	private List<BiConsumer<Integer, Integer>> resizeListeners;
	private Dimension size;
	
	public DesktopWindow(WindowConfiguration config, BiConsumer<Integer, Integer> updateViewport, boolean autoGLContext) {
		glfwSetErrorCallback((error, descriptionPointer) -> {
			var message = MemoryUtil.memUTF8(descriptionPointer);
			System.err.println("GLFW Error " + error + ": " + message);
		});

		if (!autoGLContext) {
			glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		}

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
				
		if (autoGLContext) {
			glfwMakeContextCurrent(handle);
			glfwSwapInterval(GLFW_TRUE);
		}
		
		glfwShowWindow(handle);

		size = new Dimension(config.width, config.height);
		
		resizeListeners = new ArrayList<>();
		glfwSetWindowSizeCallback(handle, this::resizeCallback);

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
	public void addResizeListener(BiConsumer<Integer, Integer> resizeListener) {
		resizeListeners.add(resizeListener);
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

	public void setPosition(int x, int y) {
		glfwSetWindowPos(handle, x, y);
	}

	public void waitUntilNotMinimized() {
		try (var memoryStack = stackPush()) {
			var widthBuffer = memoryStack.callocInt(1);
			var heightBuffer = memoryStack.callocInt(1);

			glfwGetFramebufferSize(handle, widthBuffer, heightBuffer);

			while (widthBuffer.get(0) == 0 || heightBuffer.get(0) == 0) {
				glfwGetFramebufferSize(handle, widthBuffer, heightBuffer);
				glfwWaitEvents();
			}
		}
	}

	private void resizeCallback(long handle, int width, int height) {
		size = new Dimension(width, height);
		updateViewport.accept(width, height);
		resizeListeners.forEach(resizeListener -> resizeListener.accept(width, height));
	}
	
}
