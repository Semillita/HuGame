package dev.hugame.util;

import static org.lwjgl.stb.STBImageWrite.*;

import dev.hugame.graphics.ResolvedTexture;

public class ImageWriter {
	public static void write(ResolvedTexture texture, String filePath) {
		Logger.log("ImageWrite#write");
		Logger.log("textureSize=%d, width=%d, height=%d, channels=%d".formatted(texture.buffer().capacity(), texture.width(), texture.height(), texture.channels()));
		var textureBuffer = texture.buffer();
		textureBuffer.rewind();

		stbi_flip_vertically_on_write(true);
		stbi_write_png(filePath, texture.width(), texture.height(), texture.channels(), textureBuffer, 0);
	}
}
