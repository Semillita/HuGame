package dev.hugame.graphics.shader.builder;

import java.util.function.Consumer;

public class PipelineShaderBuilder {
  private String vertexSource = null;
  private String fragmentSource = null;
  private int descriptorCount = 0;

  public PipelineShaderBuilder addVertexShader(Consumer<ShaderBuilder> callback) {
    var vertexShaderBuilder = new ShaderBuilder(this);
    callback.accept(vertexShaderBuilder);
    this.vertexSource = vertexShaderBuilder.build();

    return this;
  }

  public PipelineShaderBuilder addFragmentShader(Consumer<ShaderBuilder> callback) {
    var fragmentShaderBuilder = new ShaderBuilder(this);
    callback.accept(fragmentShaderBuilder);
    this.fragmentSource = fragmentShaderBuilder.build();

    return this;
  }

  public GeneratedShaders build() {
    return new GeneratedShaders(vertexSource, fragmentSource);
  }

  int addDescriptorBinding() {
    return descriptorCount++;
  }
}
