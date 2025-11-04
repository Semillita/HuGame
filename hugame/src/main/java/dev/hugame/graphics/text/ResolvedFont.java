package dev.hugame.graphics.text;

import dev.hugame.graphics.ResolvedTexture;

public class ResolvedFont {
	private final ResolvedTexture atlas;
	private final FontMetadata metadata;

	public ResolvedFont(ResolvedTexture atlas, FontMetadata metadata) {
		this.atlas = atlas;
		this.metadata = metadata;
	}

	public ResolvedTexture getAtlas() {
		return atlas;
	}

	public FontMetadata getMetadata() {
		return metadata;
	}
}
