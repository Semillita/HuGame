package dev.hugame.desktop.gl.renderer;

import dev.hugame.desktop.gl.GLBatch;
import dev.hugame.desktop.gl.GLRenderer;
import dev.hugame.desktop.gl.GLTextureArray;
import dev.hugame.desktop.gl.GLUtils;
import dev.hugame.desktop.gl.buffer.Buffer;
import dev.hugame.desktop.gl.buffer.UniformBuffer;
import dev.hugame.desktop.gl.shader.OpenGLShader;
import dev.hugame.desktop.gl.shader.ShaderFactory;
import dev.hugame.util.Files;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class QuadRenderer {
    private static final int QUAD_PIPELINE_VERTEX_SHADER_UNIFORM_BUFFER_BYTES = Float.BYTES * 16 * 2;

    private final OpenGLShader shader;

    private final UniformBuffer vertexShaderUniformBuffer;

    public QuadRenderer(GLRenderer renderer) {
        var quadVertexSource = Files.read("/shaders/opengl_quad_vertex_shader.glsl").orElseThrow();
        var quadFragmentSource = Files.read("/shaders/opengl_quad_fragment_shader.glsl").orElseThrow();

        this.shader = new ShaderFactory().createShader(quadVertexSource, quadFragmentSource).orElseThrow();

        this.vertexShaderUniformBuffer = new UniformBuffer(Buffer.generate());
        vertexShaderUniformBuffer.allocate(QUAD_PIPELINE_VERTEX_SHADER_UNIFORM_BUFFER_BYTES);
    }

    public void renderBatch(GLBatch batch) {
        glDisable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        var textures = batch.getTextures();

        //GLUtils.fillVBO(batch.getVboID(), batch.getVertices());
        GLUtils.fillVBO(batch.getVboID(), batch.getVertexData());
        shader.use();

        GLUtils.uploadToUniformBuffer(vertexShaderUniformBuffer, nativeBuffer -> {
            batch.getCamera().getProjectionMatrix().get(0, nativeBuffer);
            batch.getCamera().getViewMatrix().get(Float.BYTES * 16, nativeBuffer);
        });

        activateAndBindTexturesForQuadPipeline(textures);

        glBindVertexArray(batch.getVaoID());
        enableVertexAttribArrays(0, 1);

        vertexShaderUniformBuffer.bindBase(0);
        glDrawElements(GL_TRIANGLES, batch.getQuadCount() * 6, GL_UNSIGNED_INT, 0);

        disableVertexAttribArrays(0, 1);

        glBindVertexArray(0);

        unbindTextureArrays();
        shader.detach();
    }

    private void activateAndBindTexturesForModelPipline(List<GLTextureArray> textures) {
        // TODO: Should this change based on switching to texture arrays?
        for (int textureIndex = 0; textureIndex < textures.size(); textureIndex++) {
            glActiveTexture(GL_TEXTURE0 + 2 + textureIndex);
            textures.get(textureIndex).bind();
        }
    }

    private void activateAndBindTexturesForQuadPipeline(List<GLTextureArray> textures) {
        // TODO: Should this change based on switching to texture arrays?
        for (int textureIndex = 0; textureIndex < textures.size(); textureIndex++) {
            glActiveTexture(GL_TEXTURE0 + 1 + textureIndex);
            textures.get(textureIndex).bind();
        }
    }

    private void unbindTextureArrays() {
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    private void enableVertexAttribArrays(int... arrays) {
        for (var i : arrays) {
            glEnableVertexAttribArray(i);
        }
    }

    private void disableVertexAttribArrays(int... arrays) {
        for (var i : arrays) {
            glDisableVertexAttribArray(i);
        }
    }
}
