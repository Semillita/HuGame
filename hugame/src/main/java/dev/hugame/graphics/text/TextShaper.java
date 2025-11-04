package dev.hugame.graphics.text;

import java.util.ArrayList;
import java.util.List;

public class TextShaper {
	public List<PositionedGlyph> shape(String text, Font font) {
		// TODO: Use harfbuzz and access already loaded metadata about the glyphs
		var metadata = font.getMetadata();
		var metadataByGlyph = metadata.glyphs();

		var glyphs = new ArrayList<PositionedGlyph>();
		double offset = 0;
		for (var c : text.toCharArray()) {
			var glyphMetadata = metadataByGlyph.get(c);
			if (glyphMetadata == null) {
				System.out.println("Break");
			}

			if (c == ' ') {
				offset += 0.8;
			}

			var bearingX = glyphMetadata.bearingX();

			var offsetX = offset + bearingX; // Offset of the colored part
			var offsetY = glyphMetadata.bearingY() - glyphMetadata.height(); // Offset of the colored part

			glyphs.add(new PositionedGlyph(c, offsetX, offsetY, glyphMetadata.width(), glyphMetadata.height()));
			offset += glyphMetadata.advance();
		}

		return glyphs;
	}

	// TODO: To be able to optimise shaping, value should be able to be shapes that don't have a corresponding char value
	public record PositionedGlyph (char value, double offsetX, double offsetY, double width, double height) {}
}
