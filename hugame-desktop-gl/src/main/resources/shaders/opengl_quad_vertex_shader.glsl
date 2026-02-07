#version 430 core

layout(binding = 0) uniform UniformBuffer0 {
    mat4 projection;
    mat4 view;
} uniformBuffer;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTextureCoordinates;
layout(location = 2) in int inTextureIndex;
layout(location = 3) in int inTextureLayer;

layout(location = 0) out vec2 outTextureCoordinates;
layout(location = 1) out int outTextureIndex;
layout(location = 2) out int outTextureLayer;

void main()
{
    gl_Position = uniformBuffer.projection * uniformBuffer.view * vec4(inPosition, 1.0);

    outTextureCoordinates = inTextureCoordinates;
    outTextureIndex = inTextureIndex;
    outTextureLayer = inTextureLayer;
}