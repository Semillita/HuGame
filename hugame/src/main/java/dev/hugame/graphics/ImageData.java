package dev.hugame.graphics;

import java.nio.ByteBuffer;

/** Utility model for representing a loaded image. */
public record ImageData(ByteBuffer buffer, int width, int height, int channels) {

}
