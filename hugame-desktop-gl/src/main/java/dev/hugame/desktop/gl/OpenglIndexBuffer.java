package dev.hugame.desktop.gl;

public class OpenglIndexBuffer {
    private final int handle;

    public OpenglIndexBuffer(int handle) {
        this.handle = handle;
    }

    public int getHandle() {
        return handle;
    }
}
