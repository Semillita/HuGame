#version 430 core

struct Material {
	vec4 ambientColor;
};

layout(std430, binding = 0) readonly buffer materialBuffer
{
    Material materials[];
};

in vec4 fNormal;
in vec2 fTexCoords;
flat in uint fTexID;
in int fMaterialIndex;

uniform sampler2D uTextures[32];

out vec4 color;

const vec4 ambientLight = vec4(1.0, 1.0, 1.0, 1.0);

void main()
{
	vec4 ambient = materials[fMaterialIndex].ambientColor;
	vec4 light = ambientLight * 1.0;
    color = texture(uTextures[fTexID], fTexCoords) * light;
    //color = fNormal;
}