package dev.hugame.graphics;

import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteBuffer;

public class FrameBuffer {

	private final int handle;
	private final int colorBufferID;
	private final int depthBufferID;
	private final int stencilBufferID;

	public FrameBuffer() {
		handle = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, handle);

		colorBufferID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, colorBufferID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1920, 1080, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBufferID, 0);

		depthBufferID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depthBufferID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 1920, 1080, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE,
				(ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthBufferID, 0);

		stencilBufferID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, stencilBufferID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_STENCIL_INDEX, 1920, 1080, 0, GL_STENCIL_INDEX, GL_UNSIGNED_BYTE,
				(ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_TEXTURE_2D, stencilBufferID, 0);

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, handle);
	}

}
