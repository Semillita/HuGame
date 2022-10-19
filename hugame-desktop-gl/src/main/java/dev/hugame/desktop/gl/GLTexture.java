package dev.hugame.desktop.gl;

import static org.lwjgl.opengl.GL40.*;

import dev.hugame.graphics.ImageData;
import dev.hugame.graphics.Texture;
import dev.hugame.util.ImageLoader;

import static org.lwjgl.stb.STBImage.*;

/** Wrapper class for a gl texture */
public class GLTexture implements Texture {

	@Deprecated
	private int handle;
	
	private final GLTextureArray textureArray;
	private final int arrayIndex;
	private int width;
	private int height;

	public GLTexture(GLTextureArray textureArray, int arrayIndex, int width, int height) {
		this.textureArray = textureArray;
		this.arrayIndex = arrayIndex;
		this.width = width;
		this.height = height;
	}
	
	/** Creates a texture with the given file content. 
	 * 
	 * Should be replaces by a gl texture initialization outside of the constructor.*/
	@Deprecated
	public GLTexture(String content) {
		bindHandle();
		setTextureParameters();

		var data = ImageLoader.read(content, 4);
		
		if (data.buffer() != null) {
			glTexImage2D(GL_TEXTURE_2D, 0, getInternalPixelFormat(data.channels()), data.width(), data.height(), 0, getPixelFormat(data.channels()),
					GL_UNSIGNED_BYTE, data.buffer());
		}
		
		width = data.width();
		height = data.height();
		
		stbi_image_free(data.buffer());

		unbind();
		
		this.textureArray = null;
		this.arrayIndex = -1;
	}
	
	public GLTexture(ImageData imageData) {
		bindHandle();
		setTextureParameters();

		if (imageData.buffer() != null) {
			glTexImage2D(GL_TEXTURE_2D, 0, getInternalPixelFormat(imageData.channels()), imageData.width(), imageData.height(), 
					0, getPixelFormat(imageData.channels()), GL_UNSIGNED_BYTE, imageData.buffer());
		}
		
		width = imageData.width();
		height = imageData.height();
		
		stbi_image_free(imageData.buffer());

		unbind();
		
		this.textureArray = null;
		this.arrayIndex = -1;
	}

	public GLTextureArray getTextureArray() {
		return textureArray;
	}
	
	public int getArrayIndex() {
		return arrayIndex;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSlice() {
		return arrayIndex;
	}
	
	/** Returns the gl handle of this texture. */
	int getHandle() {
		return handle;
	}

	/** Binds this texture to be used. */
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, handle);
	}

	/** Unbinds this texture from being used. */
	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	private void bindHandle() {
		handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, handle);
	}

	private void setTextureParameters() {
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}

	private int getPixelFormat(int channels) {
		int format;
		switch (channels) {
		case 2:
			format = GL_RG;
			break;
		case 3:
			format = GL_RGB;
			break;
		case 4:
			format = GL_RGBA;
			break;
		default:
			format = GL_RGBA;
		}

		return format;
	}
	
	private int getInternalPixelFormat(int channels) {
		int format;
		switch (channels) {
		case 3:
			format = GL_SRGB;
			break;
		case 4:
			format = GL_SRGB_ALPHA;
			break;
		default:
			format = GL_SRGB_ALPHA;
		}

		return format;
	}

}
