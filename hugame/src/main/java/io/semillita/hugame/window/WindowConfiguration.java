package io.semillita.hugame.window;

public class WindowConfiguration {

	public String title = "jFury Application";
	public int x = -1, y = -1;
	public int width = 960, height = 540;
	
	public boolean resizable = true;
	public boolean decorated = true;
	public boolean focused = true;
	public boolean autoIconify = false;
	public boolean floating = false;
	public boolean maximized = false;
	public boolean fullscreen = false;
	public boolean windowedFullscren = false;
	public boolean centerCursor = false;
	public boolean transparentFramebuffer = false;
	public boolean focusOnShow = true;
	
	public WindowConfiguration title(String s) {
		title = s;
		return this;
	}
	
	public WindowConfiguration x(int i) {
		x = i;
		return this;
	}
	public WindowConfiguration y(int i) {
		y = i;
		return this;
	}
	public WindowConfiguration width(int i) {
		width = i;
		return this;
	}
	public WindowConfiguration height(int i) {
		height = i;
		return this;
	}
	
	public WindowConfiguration resizable(boolean b) {
		resizable = b;
		return this;
	}
	
	public WindowConfiguration decorated(boolean b) {
		decorated = b;
		return this;
	}
	
	public WindowConfiguration focused(boolean b) {
		focused = b;
		return this;
	}
	
	public WindowConfiguration autoIconify(boolean b) {
		autoIconify = b;
		return this;
	}
	
	public WindowConfiguration floating(boolean b) {
		floating = b;
		return this;
	}
	
	public WindowConfiguration maximized(boolean b) {
		maximized = b;
		return this;
	}
	
	public WindowConfiguration fullscreen(boolean b) {
		fullscreen = b;
		return this;
	}
	
	public WindowConfiguration windowedFullscren(boolean b) {
		windowedFullscren = b;
		return this;
	}
	
	public WindowConfiguration centerCursor(boolean b) {
		centerCursor = b;
		return this;
	}
	
	public WindowConfiguration transparentFramebuffer(boolean b) {
		transparentFramebuffer = b;
		return this;
	}
	
	public WindowConfiguration focusOnShow(boolean b) {
		focusOnShow = b;
		return this;
	}
	
}
