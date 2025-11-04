package dev.hugame.freetype;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.ResolvedTexture;
import dev.hugame.graphics.text.FontLoader;
import dev.hugame.graphics.text.FontMetadata;
import dev.hugame.graphics.text.ResolvedFont;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import dev.hugame.util.Files;
import dev.hugame.util.ImageLoader;
import dev.hugame.util.ImageWriter;
import dev.hugame.util.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.msdfgen.MSDFGenBitmap;
import org.lwjgl.util.msdfgen.MSDFGenBounds;
import org.lwjgl.util.msdfgen.MSDFGenTransform;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.util.msdfgen.MSDFGen.*;
import static org.lwjgl.util.msdfgen.MSDFGenExt.*;

// TODO: Don't actually have this class create a ResolvedFont since it does more than that
public class MsdfGenFontLoader implements FontLoader {
	public static ResolvedTexture FONT_TEXTURE = ImageLoader.read(Files.readBytes("/landscape.png").orElseThrow(), 4);

	private static final byte[] PINK_COLOR_BYTES = {
			(byte) 0xFF,
			(byte) 0x8E,
			(byte) 0xE4,
			(byte) 0xFF
	};

	@Override
	public Optional<ResolvedFont> load(FileHandle file) {
		try (var memoryStack = stackPush()) {
			var handleBuffer = memoryStack.mallocPointer(1);

			assertSuccess(msdf_ft_set_load_callback(name -> FreeType.getLibrary().getFunctionAddress(MemoryUtil.memByteBuffer(name, MemoryUtil.memByteBufferNT1(name).capacity() + 1))));
			assertSuccess(msdf_ft_init(handleBuffer));
			var freeTypeHandle = handleBuffer.get(0);

			if (file.location() != FileLocation.INTERNAL) {
				throw new RuntimeException("Currently not supported"); // TODO: Support
			}
			var fileContent = Files.readIntoNativeBuffer(file.path()).orElseThrow();

			assertSuccess(msdf_ft_load_font_data(freeTypeHandle, fileContent, handleBuffer));
			var fontHandle = handleBuffer.get(0);

			var aGlyph = getGlyph(fontHandle, memoryStack, 'A'); // TODO: Drop in clean-up
			var spaceGlyph = getGlyph(fontHandle, memoryStack, ' ');

			var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖabcdefghijklmnopqrstuvwxyzåäö .,:;!?".toCharArray();

			var glyphBitmaps = new ArrayList<MsdfGlyphBitmap>();
			// TODO: Make this a map from character to some metadata not containing bounds in the atlas
			var charToGlyphMetadata = new HashMap<Character, FontMetadata.GlyphMetadata>();
			// TODO: Don't use GlyphMetadata (which includes atlas bounds).
			//var glyphMetadataByChar = new HashMap<Character, FontMetadata.GlyphMetadata>();
			for (var c : chars) {
				var glyph = getGlyph(fontHandle, memoryStack, c);
				glyphBitmaps.add(glyph.bitmap);
				charToGlyphMetadata.put(c, glyph.metadata);
			}

			// Sort out the metadata - a glyph has metadata before and after the atlas is created
			var glyphAtlas = makeAtlas2(glyphBitmaps);
			var charToAtlasGlyphBounds = glyphAtlas.glyphs;
			// TODO: Once the atlas has been created, why am I creating metadata?
			var metadataByGlyph = new HashMap<Character, FontMetadata.GlyphMetadata>();
			for (var c : chars) {
				var atlasGlyphBounds = charToAtlasGlyphBounds.get(c);
				var otherMetadata = charToGlyphMetadata.get(c);

				if (atlasGlyphBounds == null) {
					System.out.println("null atlasGlyphBounds for character " + c);
					System.out.println(chars.length + ", " + charToAtlasGlyphBounds.size());
					System.out.println("Break");
				}

				var finishedMetadata = new FontMetadata.GlyphMetadata(
						atlasGlyphBounds.x,
						atlasGlyphBounds.y,
						atlasGlyphBounds.width,
						atlasGlyphBounds.height,
						otherMetadata.bearingX(),
						otherMetadata.bearingY(),
						otherMetadata.width(),
						otherMetadata.height(),
						otherMetadata.advance());
				metadataByGlyph.put(c, finishedMetadata);
			}

			ImageWriter.write(glyphAtlas.texture, "GlyphAtlas.png");

			ImageWriter.write(toResolvedTexture(aGlyph.bitmap), "MSDF-A.png");
			//stbi_flip_vertically_on_write(true);
			//stbi_write_png("msdfgen-B.png", aGlyphBitmap.width, aGlyphBitmap.height, aGlyphBitmap.channelCount, aGlyphBitmap.buffer, 0);

			nmsdf_ft_font_destroy(fontHandle);
			msdf_ft_deinit(freeTypeHandle); // TODO: Probably don't do this since I might want to load more fonts

			msdf_ft_get_load_callback().free(); // TODO: Same as above

			return Optional.of(new ResolvedFont(glyphAtlas.texture, new FontMetadata(metadataByGlyph)));
		}
	}

	// TODO: Consider if the glyph is not in the font
	// TODO: This return type MsdfGlyph should not contain FontMetadata.GlyphMetadata since the latter contains atlas bounds
	private MsdfGlyph getGlyph(long fontHandle, MemoryStack memoryStack, char value) {
		var shapeHandleBuffer = memoryStack.mallocPointer(1);
		assertSuccess(msdf_ft_font_load_glyph(fontHandle, value, MSDF_FONT_SCALING_EM_NORMALIZED, shapeHandleBuffer));
		var shapeHandle = shapeHandleBuffer.get(0);

		assertSuccess(msdf_shape_normalize(shapeHandle));
		assertSuccess(msdf_shape_edge_colors_simple(shapeHandle, 3.0)); // Try with and without this

		var bounds = MSDFGenBounds.calloc(memoryStack);
		msdf_shape_get_bounds(shapeHandle, bounds);
		var right = bounds.r();
		var left = bounds.l();
		var top = bounds.t();
		var bottom = bounds.b();
		var widthFactor = right - left;
		var heightFactor = top - bottom;

		var bearingXFactor = left;
		var bearingYFactor = top;

		// TODO: Don't use random numbers here, instead use the actual width and height
		var bitmapWidthSeed = new Random().nextInt(30);
		var bitmapWidth = 32 + bitmapWidthSeed;
		var bitmapHeightSeed = new Random().nextInt(30);
		var bitmapHeight = 32 + bitmapHeightSeed;
		var sizeFactor = 32; // Amount of pixels equal to one "em"

		var paddingFactor = 0.125; // Amount of "em" to pad the glyph with

		var bitmap = MSDFGenBitmap.calloc(memoryStack);
		assertSuccess(msdf_bitmap_alloc(MSDF_BITMAP_TYPE_MSDF, bitmapWidth, bitmapHeight, bitmap));

		var range = HuGame.MSDF_RANGE;
		assertSuccess(msdf_generate_msdf(bitmap, shapeHandle, MSDFGenTransform.calloc(memoryStack)
				.scale(
						scale -> scale
								.x(sizeFactor)
								.y(sizeFactor))
				.translation(
						translation -> translation
								.x(paddingFactor)
								.y(paddingFactor - bottom))
				.distance_mapping(
						distanceMapping -> distanceMapping
								.lower(-0.5 * range)
								.upper(0.5 * range))
		));

		var channelCountBuffer = memoryStack.mallocInt(1);
		msdf_bitmap_get_channel_count(bitmap, channelCountBuffer);
		var channelCount = channelCountBuffer.get(0);

		var pixelDataBuffer = getBitmapU8(memoryStack, bitmap, channelCount);

		var glyphBitmap = new MsdfGlyphBitmap(value, bitmapWidth, bitmapHeight, channelCount, pixelDataBuffer);

		// TODO: Don't create the final Metadata here since the atlas needs to be generated before
		//  I know the atlasOffsetX, atlasOffsetY
		var atlasXPixels = 0; //(left + paddingFactor) * sizeFactor; // this and atlasYPixels don't depend on the char pos, all point to the same char
		var atlasYPixels = 0; // paddingFactor * sizeFactor;
		var atlasWidthPixels = widthFactor * sizeFactor; // Not necessarily the case - the bitmap width/height are integers.
		var atlasHeightPixels = heightFactor * sizeFactor;
		var bearingX = bearingXFactor;
		var bearingY = bearingYFactor;
		var width = widthFactor;
		var height = heightFactor;
		var advance = bearingX + width + bearingX; // TODO: Get the actual advance
		var metadata = new FontMetadata.GlyphMetadata(
				atlasXPixels,
				atlasYPixels,
				atlasWidthPixels,
				atlasHeightPixels,
				bearingX,
				bearingY,
				width,
				height,
				advance);
		//var metadata = getMetadata(shapeHandle);

		return new MsdfGlyph(glyphBitmap, metadata);
	}

	private ByteBuffer getBitmapU8(MemoryStack memoryStack, MSDFGenBitmap bitmap, int channelCount) {
		var pointerBuffer = memoryStack.mallocPointer(1);

		assertSuccess(msdf_bitmap_get_byte_size(bitmap, pointerBuffer));
		long byteSize = pointerBuffer.get(0);

		assertSuccess(msdf_bitmap_get_pixels(bitmap, pointerBuffer));
		var pixels = MemoryUtil.memFloatBuffer(pointerBuffer.get(0), (int)byteSize >> 2);

		ByteBuffer data = MemoryUtil.memAlloc(bitmap.width() * bitmap.height() * channelCount);
		for (int y = 0; y < bitmap.height(); y++) {
			for (int x = 0; x < bitmap.width(); x++) {
				int index = (y * bitmap.width() + x) * channelCount;
				for (int c = 0; c < channelCount; c++) {
					float val;
					if (pixels.get(index + c) <= 1f && pixels.get(index + c) >= 0f) {
						val = pixels.get(index + c);
					} else {
						if (pixels.get(index + c) > 0f) {
							val = 1f;
						} else {
							val = 0;
						}
					}
					data.put(index + c, (byte)(~(int)(255.5f-255.f * val)));
				}
			}
		}

		return data;
	}

	private FontMetadata.GlyphMetadata getMetadata(long shapeHandle) {
		// TODO: Implement
		//msdf_
		return null;
	}

	private void assertSuccess(int result) {
		if (result != MSDF_SUCCESS) {
			throw new RuntimeException("[HuGame] Operation failed, error=" + result);
		}
	}

	/*private ResolvedTexture makeAtlas(List<MsdfGlyphBitmap> glyphs) {
		var startTime = System.nanoTime();
		final int maxGlyphsPerRow = 8;
		final int rows = (glyphs.size() % maxGlyphsPerRow == 0) ? (glyphs.size() / maxGlyphsPerRow) : ((glyphs.size() / maxGlyphsPerRow) + 1);
		final int columns = Math.min(maxGlyphsPerRow, glyphs.size());

		var glyphWidth = glyphs.get(0).width;
		var glyphHeight = glyphs.get(0).height;

		var atlasWidth = columns * glyphWidth;
		var atlasHeight = rows * glyphHeight;
		var atlasChannelCount = 4; //glyphs.get(0).channelCount; Force 4 channels

		var glyphChannelCount = glyphs.get(0).channelCount;

		// TODO: Delete this buffer later
		var atlasBuffer = BufferUtils.createByteBuffer(atlasWidth * atlasHeight * atlasChannelCount);
		atlasBuffer.rewind();

		for (int y = 0; y < atlasHeight; y++) {
			var row = y / glyphHeight;

			for (int x = 0; x < atlasWidth; x++) {
				var column = x / glyphWidth;

				var glyphIndex = row * columns + column;
				if (glyphIndex >= glyphs.size()) {
					for (int i = 0; i < glyphChannelCount; i++) {
						var atlasBufferIndex = (y * atlasWidth + x) * atlasChannelCount + i;
						atlasBuffer.put(atlasBufferIndex, PINK_COLOR_BYTES[i]);
					}
					if (glyphChannelCount == 3) {
						var atlasBufferIndex = (y * atlasWidth + x) * atlasChannelCount + 3;
						atlasBuffer.put(atlasBufferIndex, PINK_COLOR_BYTES[3]);
					}
					continue;
				}

				var glyph = glyphs.get(glyphIndex);
				var glyphBuffer = glyph.buffer;
				var glyphX = x % glyphWidth;
				var glyphY = y % glyphHeight;
				var pixelIndex = glyphY * glyphWidth + glyphX;

				for (int i = 0; i < glyphChannelCount; i++) {
					var glyphValue = glyphBuffer.get(pixelIndex * glyphChannelCount + i);
					var atlasBufferIndex = (y * atlasWidth + x) * atlasChannelCount + i;
					atlasBuffer.put(atlasBufferIndex, glyphValue);
				}
				if (glyphChannelCount == 3) {
					var atlasBufferIndex = (y * atlasWidth + x) * atlasChannelCount + 3;
					atlasBuffer.put(atlasBufferIndex, (byte) 0xFF);
				}
			}
		}
		var endTime = System.nanoTime();
		var elapsed = (endTime - startTime) / 1_000_000_000d;
		Logger.log("Generated MSDF atlas in " + elapsed + " seconds");
		return new ResolvedTexture(atlasBuffer, atlasWidth, atlasHeight, atlasChannelCount);
	}*/

	private MsdfAtlas makeAtlas2(List<MsdfGlyphBitmap> glyphs) {
		Logger.pushScope("MsdfGenFontLoader#makeAtlas2");
		var startTime = System.nanoTime();

		// TODO: Don't decide the row count here, instead fill in glyphs into new rows as long as needed

		var sortedGlyphs = glyphs.stream().sorted(Comparator.comparing(MsdfGlyphBitmap::height).reversed()).toList();
		int glyphWidthSum = sortedGlyphs.stream().mapToInt(MsdfGlyphBitmap::width).sum();
		Logger.log("glyphWidthSum: " + glyphWidthSum);

		var estimatedGlyphsPerRow = Math.max((int) Math.sqrt(sortedGlyphs.size()), 1);
		final var atlasWidth = (int) (glyphWidthSum * (((double) estimatedGlyphsPerRow) / sortedGlyphs.size()));

		var glyphRows = new ArrayList<List<MsdfGlyphBitmap>>();
		var glyphCounter = 0;
		while (glyphCounter < sortedGlyphs.size()) {
			var rowGlyphs = new ArrayList<MsdfGlyphBitmap>();
			int rowWidthUsed = 0;

			for (int i = glyphCounter; i < sortedGlyphs.size(); i++) {
				var glyph = sortedGlyphs.get(i);
				if (rowWidthUsed + glyph.width > atlasWidth) {
					break;
				}

				rowGlyphs.add(glyph);
				rowWidthUsed += glyph.width;
			}

			glyphRows.add(rowGlyphs);
			glyphCounter += rowGlyphs.size();
		}

		Logger.log("Glyph count: " + glyphs.size());
		Logger.log("rowCount: " + glyphRows.size());


		var glyphRowSizeSum = glyphRows.stream().mapToInt(List::size).sum();
		if (glyphRowSizeSum != glyphs.size()) {
			Logger.log("Missing " + (glyphs.size() - glyphRowSizeSum) + " glyphs");
			var uniqueGlyphCount = glyphs.stream()
					.map(MsdfGlyphBitmap::value)
					.distinct()
					.count();
			Logger.log("Of " + glyphs.size() + " glyphs, there are " + uniqueGlyphCount + " unique glyphs");
		}

		int atlasHeight = glyphRows.stream().mapToInt(glyphRow -> glyphRow.get(0).height).sum();
		var atlasChannelCount = 4;
		var glyphChannelCount = glyphs.get(0).channelCount;

		var atlasBuffer = BufferUtils.createByteBuffer(atlasWidth * atlasHeight * atlasChannelCount);
		atlasBuffer.rewind();

		Logger.log("glyphChannelCount: " + glyphChannelCount);

		Logger.log("Glyph rows:");
		for (var glyphRow : glyphRows) {
			Logger.log(glyphRow.stream().map(MsdfGlyphBitmap::value).toList().toString());
		}

		var boundsByChar = new HashMap<Character, MsdfAtlasGlyphBounds>();

		var offsetY = 0;
		for (int row = 0; row < glyphRows.size(); row++) {
			var rowGlyphs = glyphRows.get(row);

			var offsetX = 0;
			for (int glyphIndex = 0; glyphIndex < rowGlyphs.size(); glyphIndex++) {
				var glyph = rowGlyphs.get(glyphIndex);

				if (boundsByChar.containsKey(glyph.value)) {
					System.out.println("Break");
				}
				boundsByChar.put(glyph.value, new MsdfAtlasGlyphBounds(offsetX, offsetY, glyph.width, glyph.height));

				Logger.log("Adding glyph " + glyph.value);
				var glyphBuffer = glyph.buffer;

				for (int x = 0; x < glyph.width; x++) {
					var absoluteX = offsetX + x;
					for (int y = 0; y < glyph.height; y++) {
						var absoluteY = offsetY + y;

						var pixelIndex = y * glyph.width + x;
						for (int i = 0; i < atlasChannelCount; i++) {
							var glyphValue = (i < glyphChannelCount) ? glyphBuffer.get(pixelIndex * glyphChannelCount + i) : ((byte) 255);
							//var glyphValue = byteValues[i];
							var atlasBufferIndex = (absoluteY * atlasWidth + absoluteX) * atlasChannelCount + i;

							atlasBuffer.put(atlasBufferIndex, glyphValue);
						}
					}
				}

				offsetX += glyph.width;
			}

			offsetY += rowGlyphs.get(0).height;
		}

		var endTime = System.nanoTime();
		var elapsed = (endTime - startTime) / 1_000_000_000d;
		Logger.log("Generated MSDF atlas in " + elapsed + " seconds");
		Logger.popScope();

		var misMatches = sortedGlyphs.stream().map(MsdfGlyphBitmap::value)
				.filter(Predicate.not(boundsByChar::containsKey)).toList();
		if (!misMatches.isEmpty()) {
			Logger.log("Missing characters in the generated atlas: " + misMatches);
		}

		//return new MsdfAtlas(FONT_TEXTURE, boundsByChar);
		return new MsdfAtlas(new ResolvedTexture(atlasBuffer, atlasWidth, atlasHeight, atlasChannelCount), boundsByChar);
	}

	private ResolvedTexture toResolvedTexture(MsdfGlyphBitmap bitmap) {
		return new ResolvedTexture(bitmap.buffer, bitmap.width, bitmap.height, bitmap.channelCount);
	}

	private record MsdfAtlas(ResolvedTexture texture, Map<Character, MsdfAtlasGlyphBounds> glyphs) {}

	private record MsdfAtlasGlyphBounds(int x, int y, int width, int height) {}

	private record MsdfGlyph(MsdfGlyphBitmap bitmap, FontMetadata.GlyphMetadata metadata) {}

	private record MsdfGlyphMetadata(double bearingX, double bearingY, double width, double height, double advance) {}

	private record MsdfGlyphBitmap(char value, int width, int height, int channelCount, ByteBuffer buffer) {}
}
