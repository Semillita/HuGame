package dev.hugame.desktop.gl;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.hugame.graphics.Texture;
import org.lwjgl.BufferUtils;

import dev.hugame.graphics.ResolvedTexture;

import static org.lwjgl.opengl.GL45.*;

public class TextureCollector {

	private final Map<Dimension, TextureArraySpec> texturesBySize;

	public TextureCollector() {
		texturesBySize = new HashMap<>();
	}

	public GLTexture addTexture(ResolvedTexture resolvedTexture) {
		var width = resolvedTexture.width();
		var height = resolvedTexture.height();
		var dim = new Dimension(width, height);

		var textureArrayData = Optional.ofNullable(texturesBySize.get(dim)).orElseGet(() -> {
			var handle = createTextureArray();
			var textureArray = new GLTextureArray(handle, dim);
			var textureArraySpec = new TextureArraySpec(textureArray, width, height, new ArrayList<>());
			texturesBySize.put(dim, textureArraySpec);
			return textureArraySpec;
		});
		var images = textureArrayData.images();

		var index = images.size();
		images.add(resolvedTexture);

		return new GLTexture(textureArrayData.textureArray(), index, width, height);
	}

	public void generate() {
		System.out.println("Generating textures");
		System.out.println(texturesBySize);
		texturesBySize.forEach((dimension, textureArraySpec) -> {
			var handle = textureArraySpec.textureArray().getHandle();
			var images = textureArraySpec.images();
			var width = textureArraySpec.width();
			var height = textureArraySpec.height();
			
			glTextureStorage3D(handle, 1, GL_RGBA8, width, height, images.size());
			for (int i = 0; i < images.size(); i++) {
				var image = images.get(i);
				glTextureSubImage3D(handle, 0, 0, 0, i, width, height, 1, GL_RGBA, GL_UNSIGNED_BYTE, image.buffer());
			}
			
		});
	}

	public int createTextureArray() {
		System.out.println("Creating texture array");
		var handleBuffer = BufferUtils.createIntBuffer(1);
		glCreateTextures(GL_TEXTURE_2D_ARRAY, handleBuffer);
		var handle = handleBuffer.get();
		
		glBindTexture(GL_TEXTURE_2D_ARRAY, handle);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		return handle;
	}
	
	private static record TextureArraySpec(GLTextureArray textureArray, int width, int height, List<ResolvedTexture> images) {
	}

}
