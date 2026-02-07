package dev.hugame.desktop.gl.renderer;

import dev.hugame.desktop.gl.GLRenderer;
import dev.hugame.desktop.gl.GLTexture;
import dev.hugame.desktop.gl.GLTextureArray;
import dev.hugame.desktop.gl.GLUtils;
import dev.hugame.desktop.gl.buffer.*;
import dev.hugame.desktop.gl.model.OpenGLModel;
import dev.hugame.desktop.gl.shader.OpenGLShader;
import dev.hugame.desktop.gl.shader.ShaderFactory;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.InstanceData;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.model.Model;
import dev.hugame.util.Files;
import dev.hugame.util.Transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

public class ModelRenderer {
    private static final int VERTEX_SHADER_UNIFORM_BUFFER_BYTES = Float.BYTES * 16 * 2;
    private static final int FRAGMENT_SHADER_UNIFORM_BUFFER_BYTES =
            Float.BYTES * 3 + Integer.BYTES * 3;

    private final OpenGLShader shader;

    private final UniformBuffer vertexShaderUniformBuffer;
    private final UniformBuffer fragmentShaderUniformBuffer;

    private final Map<Model, List<InstanceData>> modelInstanceData;

    private final GLRenderer renderer;

    private MaterialBuffer materialBuffer;
    private PointLightBuffer pointLightBuffer;
    private int pointLightAmount = 0;
    private SpotLightBuffer spotLightBuffer;
    private int spotLightAmount = 0;
    private DirectionalLightBuffer directionalLightBuffer;
    private int directionalLightAmount = 0;

    public ModelRenderer(GLRenderer renderer) {
        var modelVertexSource = Files.read("/shaders/opengl_model_vertex_shader.glsl").orElseThrow();
        var modelFragmentSource = Files.read("/shaders/opengl_model_fragment_shader.glsl").orElseThrow();
        this.shader = new ShaderFactory().createShader(modelVertexSource, modelFragmentSource).orElseThrow();

        this.vertexShaderUniformBuffer = new UniformBuffer(Buffer.generate());
        vertexShaderUniformBuffer.allocate(VERTEX_SHADER_UNIFORM_BUFFER_BYTES);

        this.fragmentShaderUniformBuffer = new UniformBuffer(Buffer.generate());
        fragmentShaderUniformBuffer.allocate(FRAGMENT_SHADER_UNIFORM_BUFFER_BYTES);

        this.modelInstanceData = new HashMap<>();

        this.renderer = renderer;

        pointLightBuffer = PointLightBuffer.allocateNew(10);
        spotLightBuffer = SpotLightBuffer.allocateNew(10);
        directionalLightBuffer = DirectionalLightBuffer.allocateNew(1);
    }

    public void draw(Model model, Transform transform) {
        var instanceData = new InstanceData(transform);

        var instanceDataList = modelInstanceData.computeIfAbsent(model, ignored -> new ArrayList<>());
        instanceDataList.add(instanceData);
    }

    public void flush() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);

        for (var entry : modelInstanceData.entrySet()) {
            var model = entry.getKey();
            var instanceDataList = entry.getValue();

            if (!(model instanceof OpenGLModel glModel)) {
                // TODO: Make a utility service for getting
                throw new RuntimeException("Invalid type of Model: " + model.getClass());
            }

            var instanceDataArray = createInstanceDataArray(instanceDataList);
            var vaoID = glModel.getVAO();
            var vboID = glModel.getInstaceVBO();
            var eboID = glModel.getEBO();
            // TODO: Add casting to model::getTextures<T>
            var textures = glModel.getTextures()
                    .stream()
                    .map(GLTexture.class::cast)
                    .map(GLTexture::getTextureArray)
                    .toList();

            GLUtils.fillVBO(vboID, instanceDataArray);

            shader.use();

            var camera = renderer.getCamera();
            GLUtils.uploadToUniformBuffer(vertexShaderUniformBuffer, nativeBuffer -> {
                camera.getProjectionMatrix().get(0, nativeBuffer);
                camera.getViewMatrix().get(Float.BYTES * 16, nativeBuffer);
            });

            GLUtils.uploadToUniformBuffer(fragmentShaderUniformBuffer, nativeBuffer -> {
                camera.getPosition().get(0, nativeBuffer);
                nativeBuffer.putInt(Float.BYTES * 3, pointLightAmount);
                nativeBuffer.putInt(Float.BYTES * 4, spotLightAmount);
                nativeBuffer.putInt(Float.BYTES * 5, directionalLightAmount);
            });

            activateAndBindTexturesForModelPipline(textures);

            glBindVertexArray(vaoID);
            enableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);

            vertexShaderUniformBuffer.bindBase(0);
            fragmentShaderUniformBuffer.bindBase(1);

            // Binding 2 used for the texture array

            materialBuffer.bindBase(3);
            pointLightBuffer.bindBase(4);
            spotLightBuffer.bindBase(5);
            directionalLightBuffer.bindBase(6);
            glDrawElementsInstanced(GL_TRIANGLES, model.getIndexCount(), GL_UNSIGNED_INT, 0, instanceDataList.size());
            disableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);
            glBindVertexArray(0);
            unbindTextureArrays();

            shader.detach();

            instanceDataList.clear();

            camera.update();
        }

        modelInstanceData.clear();
    }

    public void updateEnvironment(Environment environment) {
        fillPointLightBuffer(environment.getPointLights());
        fillSpotLightBuffer(environment.getSpotLights());
        fillDirectionalLightBuffer(environment.getDirectionalLights());
    }

    public void setMaterials(List<Material> materials) {
        materialBuffer = MaterialBuffer.createFrom(materials);
    }

    private void fillPointLightBuffer(List<PointLight> lights) {
        if (pointLightBuffer.getMaxItems() >= lights.size()) {
            pointLightBuffer.refill(lights);
        } else {
            pointLightBuffer.fill(lights);
        }

        this.pointLightAmount = lights.size();
    }

    private void fillSpotLightBuffer(List<SpotLight> lights) {
        if (spotLightBuffer.getMaxItems() >= lights.size()) {
            spotLightBuffer.refill(lights);
        } else {
            spotLightBuffer.fill(lights);
        }

        this.spotLightAmount = lights.size();
    }

    private void fillDirectionalLightBuffer(List<DirectionalLight> lights) {
        if (directionalLightBuffer.getMaxItems() >= lights.size()) {
            directionalLightBuffer.refill(lights);
        } else {
            directionalLightBuffer.fill(lights);
        }

        this.directionalLightAmount = lights.size();
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

    private void activateAndBindTexturesForModelPipline(List<GLTextureArray> textures) {
        // TODO: Should this change based on switching to texture arrays?
        for (int textureIndex = 0; textureIndex < textures.size(); textureIndex++) {
            glActiveTexture(GL_TEXTURE0 + 2 + textureIndex);
            textures.get(textureIndex).bind();
        }
    }

    private float[] createInstanceDataArray(List<InstanceData> instanceData) {
        var instanceDataArray = new float[instanceData.size() * 16];
        for (int i = 0; i < instanceData.size(); i++) {
            var instance = instanceData.get(i);
            instance.transform().getMatrix().get(instanceDataArray, i * 16);
        }
        return instanceDataArray;
    }
}
