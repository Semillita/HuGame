package dev.hugame.graphics.text.fontatlas;

import dev.hugame.graphics.ResolvedTexture;

import java.util.Map;

public record FontAtlas(ResolvedTexture texture, Map<Character, GlyphBounds> glyphs) {
	public record GlyphBounds(int x, int y, int width, int height) {}
}
