package dev.hugame.vulkan.pipeline.shader;

import static org.lwjgl.util.shaderc.Shaderc.*;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class ShaderUtils {
  private static final String SHADER_ENTRYPOINT_METHOD_NAME = "main";

  public static ByteBuffer compile(String source, ShaderType shaderType) {
    var compilerHandle = shaderc_compiler_initialize();
    if (compilerHandle == MemoryUtil.NULL) {
      throw new RuntimeException("[HuGame] Failed to create Shaderc compiler");
    }

    var compilationResult =
        shaderc_compile_into_spv(
            compilerHandle,
            source,
            shaderType.getShadercCode(),
            "File",
            SHADER_ENTRYPOINT_METHOD_NAME,
            MemoryUtil.NULL);

    if (compilationResult == MemoryUtil.NULL) {
      throw new RuntimeException("[HuGame] Failed to compile shader source code");
    }

    if (shaderc_result_get_compilation_status(compilationResult)
        != shaderc_compilation_status_success) {
      throw new RuntimeException(
          "[HuGame] Failed to compile shader source code: "
              + shaderc_result_get_error_message(compilationResult));
    }

    shaderc_compiler_release(compilerHandle);

    return shaderc_result_get_bytes(compilationResult);
  }
}
