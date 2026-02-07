package dev.hugame.desktop.gl.buffer;

import static org.lwjgl.opengl.GL43.*;

public class UniformBuffer extends Buffer {
    private int size = -1;

    public UniformBuffer(int handle) {
        super(handle, GL_UNIFORM_BUFFER);
    }

    public void allocate(int size) {
        this.size = size;
        super.bind();
        super.bufferData(size);
    }

    public void bindBase(int index) {
        glBindBufferBase(GL_UNIFORM_BUFFER, index, super.getHandle());
    }

    public int getSize() {
        return size;
    }
}
