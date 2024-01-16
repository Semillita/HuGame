package dev.hugame.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import org.lwjgl.BufferUtils;

import dev.hugame.graphics.ImageData;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import static org.lwjgl.system.MemoryStack.stackPush;

public class ImageLoader {

	public static ImageData read(String content, int forcedChannels) {
		try (var memoryStack = stackPush()) {
			var contentBuffer = getContentBuffer(content, memoryStack);

			var widthBuffer = memoryStack.callocInt(1);
			var heightBuffer = memoryStack.callocInt(1);
			var channelsBuffer = memoryStack.callocInt(1);
			var imageBuffer = stbi_load_from_memory(contentBuffer, widthBuffer, heightBuffer, channelsBuffer,
					forcedChannels);

			return new ImageData(imageBuffer, widthBuffer.get(0), heightBuffer.get(0), forcedChannels);
		}
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

	private static ByteBuffer getContentBuffer(String content, MemoryStack memoryStack) {
		var contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
		return memoryStack.calloc(contentBytes.length).put(contentBytes).flip();
	}

}
