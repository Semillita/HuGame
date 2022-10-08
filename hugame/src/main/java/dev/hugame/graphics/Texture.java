package dev.hugame.graphics;

import static org.lwjgl.opengl.GL40.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.lwjgl.BufferUtils;

import dev.hugame.util.ImageLoader;

import static org.lwjgl.stb.STBImage.*;

/** Wrapper class for a gl texture */
public class Texture {

	private int handle;

	/** Creates a texture with the given file content. 
	 * 
	 * Should be replaces by a gl texture initialization outside of the constructor.*/
	@Deprecated
	public Texture(String content) {
		bindHandle();
		setTextureParameters();

		var data = ImageLoader.read(content, 4);
		
		if (data.buffer() != null) {
			glTexImage2D(GL_TEXTURE_2D, 0, getInternalPixelFormat(data.channels()), data.width(), data.height(), 0, getPixelFormat(data.channels()),
					GL_UNSIGNED_BYTE, data.buffer());
		}
		stbi_image_free(data.buffer());

		unbind();
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
