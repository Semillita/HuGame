package dev.hugame.graphics.shader.builder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public record VertexAttributes(List<VertexAttribute> attributes) {
    @SuppressWarnings("unchecked")
    public static VertexAttributes fromClass(Class<?> attributeClass) {
        var attributes = new ArrayList<VertexAttribute>();

        for (var field : attributeClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            var fieldType = field.getType();
            if (!ShaderType.class.isAssignableFrom(fieldType) || ShaderType.class.equals(fieldType)) {
                throw new RuntimeException("Invalid shader field type: " + fieldType);
            }

            var binding = (field.getAnnotation(PerInstance.class) != null) ? 1 : 0;

            attributes.add(new VertexAttribute((Class<? extends ShaderType>) field.getType(), binding));
        }

        return new VertexAttributes(attributes);
    }
}
