package dev.hugame.desktop.gl;

import java.awt.Dimension;

import static org.lwjgl.opengl.GL45.*;

public class GLTextureArray {

	private int width, height, depth;
	private int handle;
	
	public GLTextureArray(int handle, Dimension dimension) {
		this.handle = handle;
		this.width = dimension.width;
		this.height = dimension.height;
		this.depth = 0;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public int getHandle() {
		return handle;
	}
	
	public void create(int handle) {
		this.handle = handle;
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D_ARRAY, handle);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof GLTextureArray)) {
			return false;
		}
		
		var textureArray = (GLTextureArray) object;
		
		return this.handle == textureArray.getHandle();
	}
	
}
