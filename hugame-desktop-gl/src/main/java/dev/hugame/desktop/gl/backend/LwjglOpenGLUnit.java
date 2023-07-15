package dev.hugame.desktop.gl.backend;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL45.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.lwjgl.BufferUtils;

import dev.hugame.desktop.gl.GLTexture;
import dev.hugame.graphics.ImageData;

/**
 * Interface for interacting with the LWJGL OpenGL bindings
 */
public class LwjglOpenGLUnit {
	public VertexArrayHandle createVertexArray() {
		var pointer = glGenVertexArrays();

		return () -> pointer;
	}

	public VertexBufferHandle createVertexBuffer(int sizeInBytes) {
		var pointer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, pointer);
		glBufferData(GL_ARRAY_BUFFER, sizeInBytes, GL_DYNAMIC_DRAW);

		return () -> pointer;
	}

	public IndexBufferHandle createIndexBuffer(int amount) {
		var pointer = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, amount * 4, GL_STATIC_DRAW);

		return () -> pointer;
	}

	public void fillIndexBuffer(IndexBufferHandle handle, int[] indices) {
		var pointer = handle.getPointer();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointer);
		glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indices);
	}

	public void setVertexAttributePointers(VertexArrayHandle vao, List<VertexAttributePointerLayout> layouts) {
		glBindVertexArray(vao.getPointer());

		for (int index = 0; index < layouts.size(); index++) {
			var layout = layouts.get(index);

			glVertexAttribPointer(index, layout.size(), layout.type().getCode(), layout.normalized(), layout.stride(),
					layout.offset());
			glEnableVertexAttribArray(index);
			glVertexAttribDivisor(index, layout.divisor());
		}
	}

	public TextureArrayHandle createTextureArray() {
		var pointerBuffer = BufferUtils.createIntBuffer(1);
		glCreateTextures(GL_TEXTURE_2D_ARRAY, pointerBuffer);
		var pointer = pointerBuffer.get();

		return () -> pointer;
	}

	public void setTextureParameters(TextureArrayHandle handle) {
		bindTextureArray(handle);
		// TODO: Make method accept 1 or 2
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}

	public void bindTextureArray(TextureArrayHandle handle) {
		glBindTexture(GL_TEXTURE_2D_ARRAY, handle.getPointer());
	}

}
