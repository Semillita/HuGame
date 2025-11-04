package dev.hugame.freetype;

import dev.hugame.graphics.text.FontLoader;
import dev.hugame.graphics.text.ResolvedFont;
import dev.hugame.graphics.text.ResolvedGlyphMetrics;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import dev.hugame.util.Files;
import dev.hugame.util.Logger;
import org.lwjgl.util.freetype.FT_Face;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.lwjgl.util.freetype.FreeType.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class FreeTypeFontLoader implements FontLoader {
	private static final String SUPPORTED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvw";
	private static final String THICKNESS_SCALE = " '.o*&$%@#";

	@Override
	public Optional<ResolvedFont> load(FileHandle file) {
		try (var memoryStack = stackPush()) {
			var freeTypeHandleBuffer = memoryStack.callocPointer(1);

			var initializeFreeTypeResult = FT_Init_FreeType(freeTypeHandleBuffer);
			if (initializeFreeTypeResult != FT_Err_Ok) {
				throw new RuntimeException("[HuGame] Failed to initialize FreeType: error=" + initializeFreeTypeResult);
			}
			Logger.log("Successfully initialized FreeType");
			var freeTypeHandle = freeTypeHandleBuffer.get();

			if (file.location() != FileLocation.INTERNAL) {
				throw new RuntimeException("Currently not supported"); // TODO: Support
			}
			var fileContent = Files.readIntoNativeBuffer(file.path()).orElseThrow();

			var faceHandleBuffer = memoryStack.callocPointer(1);
			var newFaceResult = FT_New_Memory_Face(freeTypeHandle, fileContent, 0, faceHandleBuffer);
			if (newFaceResult != FT_Err_Ok) {
				throw new RuntimeException("[HuGame] Failed to create font face: error=" + newFaceResult);
			}
			Logger.log("Successfully created new font face");
			var faceHandle = faceHandleBuffer.get();

			var face = FT_Face.create(faceHandle);

			var setCharSizeResult = FT_Set_Char_Size(face, 0, 16 * 64, 0, 200);
			if (setCharSizeResult != FT_Err_Ok) {
				throw new RuntimeException("[HuGame] Failed to set char size: error=" + setCharSizeResult);
			}

			var fontFormat = FT_Get_Font_Format(face);

			var postscriptName = FT_Get_Postscript_Name(face);

			var glyphSlot = face.glyph();

			for (char c : SUPPORTED_CHARS.toCharArray()) {
				var glyphIndex = FT_Get_Char_Index(face, c);

				var loadGlyphResult = FT_Load_Glyph(face, glyphIndex, FT_LOAD_DEFAULT);
				if (loadGlyphResult != FT_Err_Ok) {
					throw new RuntimeException("[HuGame] Failed to load glyph: char=" + c);
				}

				var metrics = glyphSlot.metrics();
				var resolvedMetrics = new ResolvedGlyphMetrics(metrics.width(), metrics.height(), metrics.horiBearingX(), metrics.horiBearingY(),
						metrics.horiAdvance());

				var format = GlyphFormat.fromValue(glyphSlot.format());
				Logger.log("Glyph format " + format);

				if (format != GlyphFormat.BITMAP) {
					FT_Render_Glyph(glyphSlot, FT_RENDER_MODE_NORMAL);
				}
				var vals = new ArrayList<Integer>();
				var bitmap = glyphSlot.bitmap();
				var width = bitmap.width();
				var height = bitmap.rows();
				Logger.log("Created bitmap with width " + width + " and height " + height);
				var buffer = bitmap.buffer(width * height);
				Logger.log("Created bitmap");
				buffer.rewind();
				for (var row = 0; row < height; row++) {
					for (var col = 0; col < width; col++) {
						var byteIndex = row * width + col;
						var val = buffer.get(byteIndex) & 0xFF;
						vals.add(val);
					}
				}
				Logger.log("Bitmap values: " + vals);
				for (var row = 0; row < height; row++) {
					for (var col = 0; col < width; col++) {
						var byteIndex = row * width + col;
						var val = vals.get(byteIndex);
						System.out.print(getChar(val));
					}
					System.out.println();
				}

				Logger.log("Metrics: " + resolvedMetrics);
			}


		}
		return Optional.empty();
	}

	private char getChar(int thickness) {
		var stepSize = 256 / THICKNESS_SCALE.length();
		var charIndex = Math.min(thickness / stepSize, THICKNESS_SCALE.length() - 1);
		//System.out.println("Grabbing " + charIndex + " with thickness = " + thickness + ", stepSize = " + stepSize);

		return THICKNESS_SCALE.charAt(charIndex);
	}

	private enum GlyphFormat {
		NONE(FT_GLYPH_FORMAT_NONE),
		COMPOSITE(FT_GLYPH_FORMAT_COMPOSITE),
		BITMAP(FT_GLYPH_FORMAT_BITMAP),
		OUTLINE(FT_GLYPH_FORMAT_OUTLINE),
		PLOTTER(FT_GLYPH_FORMAT_PLOTTER),
		SVG(FT_GLYPH_FORMAT_SVG);

		private final int value;

		GlyphFormat(int value) {
			this.value = value;
		}

		public static GlyphFormat fromValue(int value) {
			return Arrays.stream(values())
					.filter(format -> format.value == value)
					.findAny()
					.orElseThrow();
		}
	}
}
