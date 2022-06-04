package io.semillita.hugame.graphics;

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

import static org.lwjgl.stb.STBImage.*;

public class Texture {

	private int handle;

	public Texture(String content) {
		try {
			var contentBuffer = getContentBuffer(content);

			bindHandle();
			setTextureParameters();

			IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
			IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
			IntBuffer channelsBuffer = BufferUtils.createIntBuffer(1);
			ByteBuffer imageBuffer = stbi_load_from_memory(contentBuffer, widthBuffer, heightBuffer, channelsBuffer, 0);

			int pixelFormat = (channelsBuffer.get(0) == 3) ? GL_RGB : GL_RGBA;

			if (imageBuffer != null) {
				glTexImage2D(GL_TEXTURE_2D, 0, getInternalPixelFormat(channelsBuffer), widthBuffer.get(0), heightBuffer.get(0), 0, getPixelFormat(channelsBuffer),
						GL_UNSIGNED_BYTE, imageBuffer);
			} else {
				System.out.println("imageBuffer is null");
			}

			stbi_image_free(imageBuffer);

			unbind();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	int getHandle() {
		return handle;
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, handle);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	private ByteBuffer getContentBuffer(String content) throws UnsupportedEncodingException {
		var contentBytes = content.getBytes("ISO-8859-1");
		return BufferUtils.createByteBuffer(contentBytes.length).put(contentBytes).flip();
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

	private int getPixelFormat(IntBuffer channelsBuffer) {
		int format;
		switch (channelsBuffer.get(0)) {
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
	
	private int getInternalPixelFormat(IntBuffer channelsBuffer) {
		int format;
		switch (channelsBuffer.get(0)) {
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
