#version 450

layout(binding = 0) uniform ViewProjection {
    mat4 view;
    mat4 projection;
} uniformBuffer;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTextureCoordinates;
layout(location = 3) in int inMaterialIndex;
layout(location = 4) in mat4 inTransform;

layout(location = 0) out vec3 position;
layout(location = 1) out vec3 normal;
layout(location = 2) out vec2 textureCoordinates;
layout(location = 3) flat out int materialIndex;

void main() {
    gl_Position = uniformBuffer.projection * uniformBuffer.view * inTransform * vec4(inPosition, 1.0);

    position = vec3(inTransform * vec4(inPosition, 1.0));
    normal = inNormal; // TODO: Normal matrix
    textureCoordinates = inTextureCoordinates;
    materialIndex = inMaterialIndex;
}