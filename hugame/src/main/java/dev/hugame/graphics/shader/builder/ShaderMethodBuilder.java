package dev.hugame.graphics.shader.builder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderMethodBuilder {
    private static final String INDENTATION = "    ";
    private final String name;
    private final StringBuilder stringBuilder = new StringBuilder();

    private String returnType = "void";
    private List<Parameter> parameters;
    private String methodBody;

    private record Parameter(String name, String type) {}

    public ShaderMethodBuilder(String name) {
        this.name = name;
    }

    public ShaderMethodBuilder setReturnType(ShaderType returnType) {
        this.returnType = getGlslTypeName(returnType);

        return this;
    }

    public ShaderMethodBuilder addParameterType(String name, ShaderType parameterType) {
        parameters.add(new Parameter(name, getGlslTypeName(parameterType)));

        return this;
    }

    public ShaderMethodBuilder addMethodBody(String methodBody) {
        this.methodBody = Arrays.stream(methodBody.split("\n"))
                .map(line -> INDENTATION + line)
                .collect(Collectors.joining("\n"));

        return this;
    }

    public String build() {
        stringBuilder.append(returnType).append(" ").append(name).append("() {\n");
        stringBuilder.append(methodBody);
        stringBuilder.append("\n}");

        return stringBuilder.toString();
    }

    private static String getGlslTypeName(ShaderType shaderType) {
        return shaderType.getClass().getAnnotation(ShaderType.GlslType.class).value();
    }
}
