#version 450

layout(binding = 1) uniform sampler2DArray[32] uTextures;

layout(location = 0) in vec2 textureCoordinates;
layout(location = 1) flat in int textureIndex;
layout(location = 2) flat in int textureLayer;

layout(location = 0) out vec4 color;

void main()
{
    color = texture(uTextures[textureIndex], vec3(textureCoordinates, textureLayer));
}