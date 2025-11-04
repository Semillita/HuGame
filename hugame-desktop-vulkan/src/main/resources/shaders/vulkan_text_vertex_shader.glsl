#version 450

layout(binding = 0) uniform ViewProjection {
    mat4 view;
    mat4 projection;
} uniformBuffer;

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec4 inColor;
layout (location = 2) in vec2 inTextureCoordinates;
layout (location = 3) in int inTextureIndex;
layout (location = 4) in int inTextureLayer;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec2 outTextueCoordinates;
layout(location = 2) out int outTextureIndex;
layout(location = 3) out int outTextureLayer;

void main()
{
    outTextueCoordinates = inTextureCoordinates;
    outTextureIndex = inTextureIndex;
    outTextureLayer = inTextureLayer;

    vec4 position = uniformBuffer.projection * uniformBuffer.view * vec4(inPosition.x, inPosition.y, inPosition.z, 1.0);
    gl_Position = vec4(position.x, position.y, -position.z, position.w);
}