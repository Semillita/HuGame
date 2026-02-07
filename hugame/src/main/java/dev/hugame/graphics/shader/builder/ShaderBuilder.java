package dev.hugame.graphics.shader.builder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShaderBuilder {
  private static final String INDENTATION = "    ";
  private final PipelineShaderBuilder pipelineShaderBuilder;
  private final List<Class<?>> structs = new ArrayList<>();
  private final List<Constant> constants = new ArrayList<>();
  private UniformBuffer uniformBuffer = null;
  private Class<?> inputClass = null;
  private Class<?> outputClass = null;
  private List<Method> methods = new ArrayList<>();

  private final StringBuilder stringBuilder = new StringBuilder();

  private record Constant(String name, String value) {}

  private record UniformBuffer(String name, Class<?> structClass, int binding) {}

  private record Field(String name, String type) {}

    private record Method(String name, Consumer<ShaderMethodBuilder> callback) {}

  ShaderBuilder(PipelineShaderBuilder pipelineShaderBuilder) {
    this.pipelineShaderBuilder = pipelineShaderBuilder;
  }

  public ShaderBuilder addStruct(Class<?> structClass) {
    structs.add(structClass);

    return this;
  }

  // TODO: Maybe make it a class of constant so they can be referenced through fields with type
  // safety
  public ShaderBuilder addConstant(String name, float value) {
    constants.add(new Constant(name, String.valueOf(value)));

    return this;
  }

  public ShaderBuilder addUniformBuffer(String name, Class<?> structClass) {
    uniformBuffer =
        new UniformBuffer(name, structClass, pipelineShaderBuilder.addDescriptorBinding());

    return this;
  }

  public ShaderBuilder addSampler2DArray(String name, int arraySize) {
        // TODO: Implement

      return this;
  }

  public ShaderBuilder addInput(Class<?> inputClass) {
    this.inputClass = inputClass;

    return this;
  }

  public ShaderBuilder addOutput(Class<?> outputClass) {
      this.outputClass = outputClass;

      return this;
  }

  public ShaderBuilder addMethod(String name, Consumer<ShaderMethodBuilder> callback) {
      methods.add(new Method(name, callback));

      return this;
  }

  public String build() {
    stringBuilder
        .append("#version 450\n")
        .append("#extension GL_EXT_nonuniform_qualifier : require\n\n");

    appendStructs();
    appendConstants();
    appendUniformBuffer();
    appendInput();
    appendOutput();
    appendMethods();

    return stringBuilder.toString();
  }

  private void appendStructs() {
    if (structs.isEmpty()) {
      return;
    }

    for (var structClass : structs) {
      stringBuilder.append("struct ").append(structClass.getSimpleName()).append(" {\n");

      for (var field : getFields(structClass)) {
        stringBuilder
            .append(INDENTATION)
            .append(field.type)
            .append(" ")
            .append(field.name)
            .append(";\n");
      }

      stringBuilder.append("};\n\n");
    }
  }

  private void appendConstants() {
    if (constants.isEmpty()) {
      return;
    }

    for (var constant : constants) {
      stringBuilder
          .append("const float ")
          .append(constant.name)
          .append(" = ")
          .append(constant.value)
          .append(";\n");
    }

    stringBuilder.append("\n");
  }

  private void appendUniformBuffer() {
    if (uniformBuffer == null) {
      return;
    }

    stringBuilder
        .append("layout(binding = ")
        .append(uniformBuffer.binding)
        .append(") uniform UniformBuffer {\n");

    for (var field : getFields(uniformBuffer.structClass)) {
      stringBuilder
          .append(INDENTATION)
          .append(field.type)
          .append(" ")
          .append(field.name)
          .append(";\n");
    }

    stringBuilder.append("} ").append(uniformBuffer.name).append(";\n\n");
  }

  private void appendInput() {
    if (inputClass == null) {
      return;
    }

    int location = 0;

    for (var field : getFields(inputClass)) {
      stringBuilder
          .append("layout(location = ")
          .append(location++)
          .append(") in ")
          .append(field.type)
          .append(" ")
          .append(field.name)
          .append(";\n");
    }

    stringBuilder.append("\n");
  }

  private void appendOutput() {
      if (outputClass == null) {
          return;
      }

      int location = 0;

      for (var field : getFields(outputClass)) {
          var potentiallyFlat = (field.type.equals("int")) ? "flat " : "";

          stringBuilder
                  .append("layout(location = ")
                  .append(location++)
                  .append(") ")
                  .append(potentiallyFlat)
                  .append("out ")
                  .append(field.type)
                  .append(" ")
                  .append(field.name)
                  .append(";\n");
      }

      stringBuilder.append("\n");
  }

  private void appendMethods() {
      for (var method : methods) {
        var shaderMethodBuilder = new ShaderMethodBuilder(method.name);
        method.callback.accept(shaderMethodBuilder);

        stringBuilder.append(shaderMethodBuilder.build()).append("\n\n");
      }
  }

  private List<Field> getFields(Class<?> structClass) {
    var fields = new ArrayList<Field>();

    for (var field : structClass.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      var fieldType = field.getType();
      if (!ShaderType.class.isAssignableFrom(fieldType)) {
        throw new RuntimeException("Invalid shader field type: " + fieldType);
      }

      var glslTypeName = fieldType.getAnnotation(ShaderType.GlslType.class).value();

      fields.add(new Field(field.getName(), glslTypeName));
    }

    return fields;
  }
}
