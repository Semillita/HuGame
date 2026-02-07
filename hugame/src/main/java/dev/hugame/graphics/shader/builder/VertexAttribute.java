package dev.hugame.graphics.shader.builder;

public record VertexAttribute(Class<? extends ShaderType> type, int binding) {
    public static int PER_VERTEX = 0;
    public static int PER_INSTANCE = 1;
}
