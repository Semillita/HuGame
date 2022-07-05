package io.semillita.hugame.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL;

import io.semillita.hugame.graphics.Graphics;

public class Window {

	static {
		glfwInit();
	}
	
	private final long handle;
	private Runnable onRender;
	private Supplier<Boolean> onClose;
	private WindowConfiguration config;
	
	public Graphics graphics;
	
	public Window(WindowConfiguration config) {
		this.config = config;
		
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
		
		//graphics = new Graphics();
	}
	
	public void setVisible(boolean visible) {
		
	}

	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void requestAttention() {
		// TODO Auto-generated method stub
		
	}

	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	public long getHandle() {
		return handle;
	}
	
	void onRender() {
		onRender.run();
	}
	
	public void update() {
//		graphics.update();
	}
	
	public void pollEvents() {
		glfwPollEvents();
	}
	
	public void clear() {
		glClearColor(1, 1, 0.5f, 1);	
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void swapBuffers() {
		glfwSwapBuffers(handle);
	}
	
	public boolean shouldClose() {
		return glfwWindowShouldClose(handle);
	}
	
	public boolean onClose() {
		return (onClose != null) ? onClose.get() : true;
	}
	
}
