package dev.hugame.graphics.renderers.model;

import dev.hugame.graphics.shader.builder.GeneratedShaders;
import dev.hugame.graphics.shader.builder.PipelineShaderBuilder;
import dev.hugame.graphics.shader.builder.ShaderType;

public class ModelPipelineShaderGenerator {
  public static GeneratedShaders createShader() {
    return new PipelineShaderBuilder()
        .addVertexShader(
            vertexShader ->
                vertexShader
                    .addUniformBuffer("uniformBuffer", VertexShaderUniformBuffer.class)
                    .addInput(VertexShaderInput.class)
                    .addOutput(VertexShaderOutput.class)
                        .addMethod("main", mainMethod ->
                                mainMethod.addMethodBody(
                                        """
                                        gl_Position = uniformBuffer.projection * uniformBuffer.view * inTransform * vec4(inPosition, 1.0);
                                        
                                        position = vec3(inTransform * vec4(inPosition, 1.0));
                                        normal = inNormal; // TODO: Normal matrix
                                        textureCoordinates = inTextureCoordinates;
                                        materialIndex = inMaterialIndex;
                                        """)))
        .addFragmentShader(
            fragmentShader ->
                fragmentShader
                    .addStruct(Material.class)
                    .addConstant("ambientStrength", 0.5f)
                    .addConstant("diffuseStrength", 0.5f)
                    .addConstant("specularStrength", 0.3f)
                    .addUniformBuffer("uniformBuffer", FragmentShaderUniformBuffer.class))
        .build();
  }

  // TODO: Maybe make the field types StructField<Integer> where StructField extends some
  // Value<Integer>
  private static final class Material {
    static ShaderType.Vector3 ambient;
    static ShaderType.Integer albedoMapTextureIndex;

    static ShaderType.Vector3 diffuse;
    static ShaderType.Integer normalMapTextureIndex;

    static ShaderType.Vector3 specular;
    static ShaderType.Integer specularMapTextureIndex;

    static ShaderType.Float shininess;
    static ShaderType.Integer albedoMapTextureLayer;
    static ShaderType.Integer normalMapTextureLayer;
    static ShaderType.Integer specularMapTextureLayer;
  }

  private static final class VertexShaderUniformBuffer {
    static ShaderType.Matrix4 view;
    static ShaderType.Matrix4 projection;
  }

  private static final class FragmentShaderUniformBuffer {
    static ShaderType.Vector3 cameraPosition;
    static ShaderType.Integer pointLightAmount;
    static ShaderType.Integer spotLightAmount;
    static ShaderType.Integer directionalLightAmount;
  }

  private static final class VertexShaderInput {
    static ShaderType.Vector3 inPosition;
    static ShaderType.Vector3 inNormal;
    static ShaderType.Vector2 inTextureCoordinates;
    static ShaderType.Integer inMaterialIndex;
    static ShaderType.Matrix4 inTransform;
  }

    private static final class VertexShaderOutput {
        static ShaderType.Vector3 outPosition;
        static ShaderType.Vector3 outNormal;
        static ShaderType.Vector2 outTextureCoordinates;
        static ShaderType.Integer outMaterialIndex;
    }
}
