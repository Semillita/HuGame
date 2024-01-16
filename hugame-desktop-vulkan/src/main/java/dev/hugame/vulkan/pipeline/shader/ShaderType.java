package dev.hugame.vulkan.pipeline.shader;

import static org.lwjgl.util.shaderc.Shaderc.*;

public enum ShaderType {
  VERTEX(shaderc_glsl_vertex_shader),
  FRAGMENT(shaderc_glsl_fragment_shader);

  private final int shadercCode;

  ShaderType(int shadercCode) {
    this.shadercCode = shadercCode;
  }

  public int getShadercCode() {
    return shadercCode;
  }
}
