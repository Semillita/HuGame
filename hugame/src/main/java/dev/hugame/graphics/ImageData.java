package dev.hugame.graphics;

import java.nio.ByteBuffer;

public record ImageData(ByteBuffer buffer, int width, int height, int channels) {

}
