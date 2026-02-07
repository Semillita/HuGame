package dev.hugame.graphics.shader.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ShaderType {
  @GlslType("int")
  public static final class Integer extends ShaderType {}

  @GlslType("float")
  public static final class Float extends ShaderType {}

  @GlslType("vec2")
  public static final class Vector2 extends ShaderType {}

  @GlslType("vec3")
  public static final class Vector3 extends ShaderType {}

  @GlslType("mat4")
  public static final class Matrix4 extends ShaderType {}

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface GlslType {
    String value();
  }
}
