package dev.hugame.graphics;

import java.nio.ByteBuffer;

/** Utility model for representing a loaded image. */
// TODO: Clear buffer at some point
public record ResolvedTexture(ByteBuffer buffer, int width, int height, int channels) {

}
