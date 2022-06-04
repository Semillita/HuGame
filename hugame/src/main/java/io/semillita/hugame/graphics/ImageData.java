package io.semillita.hugame.graphics;

import java.nio.ByteBuffer;

public record ImageData(ByteBuffer data, int width, int height, int channels) {

}
