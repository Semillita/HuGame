#version 450

layout(binding = 0) uniform ViewProjection {
    mat4 view;
    mat4 projection;
} uniformBuffer;

layout (location=0) in vec3 inPosition;
layout (location=1) in vec2 inTextureCoordinates;
layout (location=2) in int inTextureIndex;
layout (location=3) in int inTextureLayer;

layout(location = 0) out vec2 outTextueCoordinates;
layout(location = 1) out int outTextureIndex;
layout(location = 2) out int outTextureLayer;

void main()
{
    vec4 position = uniformBuffer.projection * uniformBuffer.view * vec4(inPosition, 1.0);
    gl_Position = vec4(position.x, position.y, -position.z, position.w);

    outTextueCoordinates = inTextureCoordinates;
    outTextureIndex = inTextureIndex;
    outTextureLayer = inTextureLayer;

}