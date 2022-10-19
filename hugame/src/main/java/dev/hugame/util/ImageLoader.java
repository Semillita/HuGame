package dev.hugame.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import dev.hugame.graphics.ImageData;

import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class ImageLoader {

	public static ImageData read(String content, int forcedChannels) {
		var contentBuffer = getContentBuffer(content);

		IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
		IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
		IntBuffer channelsBuffer = BufferUtils.createIntBuffer(1);
		ByteBuffer imageBuffer = stbi_load_from_memory(contentBuffer, widthBuffer, heightBuffer, channelsBuffer,
				forcedChannels);

		return new ImageData(imageBuffer, widthBuffer.get(0), heightBuffer.get(0), forcedChannels);
	}

	public static ImageData read(byte[] content, int forcedChannels) {
		var buffer = BufferUtils.createByteBuffer(content.length);
		buffer.put(content).flip();
		return read(buffer, forcedChannels);
	}

	public static ImageData read(ByteBuffer content, int forcedChannels) {
		IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
		IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
		IntBuffer channelsBuffer = BufferUtils.createIntBuffer(1);
		ByteBuffer imageBuffer = stbi_load_from_memory(content, widthBuffer, heightBuffer, channelsBuffer,
				forcedChannels);

		return new ImageData(imageBuffer, widthBuffer.get(0), heightBuffer.get(0), forcedChannels);
	}

	private static ByteBuffer getContentBuffer(String content) {
		try {
			var contentBytes = content.getBytes("ISO-8859-1");
			return BufferUtils.createByteBuffer(contentBytes.length).put(contentBytes).flip();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("Noooo");
		}
	}

}
