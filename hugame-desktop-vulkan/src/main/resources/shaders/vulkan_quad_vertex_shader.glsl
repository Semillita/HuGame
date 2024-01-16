#version 450
layout (location=0) in vec3 inPosition;
layout (location=1) in vec2 inTextureCoordinates;
layout (location=2) in int inTextureIndex;
layout (location=3) in int inTextureLayer;

layout(binding = 0) uniform ViewProjection {
    mat4 view;
    mat4 projection;
} ubo;

layout(location = 0) out vec2 outTextueCoordinates;
layout(location = 1) out int outTextureIndex;
layout(location = 2) out int outTextureLayer;

void main()
{
    outTextueCoordinates = inTextureCoordinates;
    outTextureIndex = inTextureIndex;
    outTextureLayer = inTextureLayer;

    vec4 position = ubo.projection * ubo.view * vec4(inPosition.x, inPosition.y, inPosition.z, 1.0);
    gl_Position = vec4(position.x, position.y, -position.z, position.w);
}