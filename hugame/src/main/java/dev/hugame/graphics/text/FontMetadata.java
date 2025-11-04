package dev.hugame.graphics.text;

import java.util.Map;

public record FontMetadata(Map<Character, GlyphMetadata> glyphs) {
	public record GlyphMetadata(
			double atlasX, // The glyph bitmap's X position on the font atlas
			double atlasY, // The glyph bitmap's Y position on the font atlas
			double atlasWidth, // The glyph bitmap's width on the font atlas
			double atlasHeight, // The glyph bitmap's height on the font atlas
			double bearingX, // The glyph's X-bearing in em units
			double bearingY, // The glyph's Y-bearing in em units
			double width, // The glyph's width in em units
			double height, // The glyph's height in em units
			double advance // The glyph's advance in em units
	) {}
}
