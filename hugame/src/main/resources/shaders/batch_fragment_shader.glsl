#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexId;

uniform sampler2D uTextures[32];

out vec4 color;

void main()
{
	int id = int(fTexId);
    color = fColor * texture(uTextures[id], fTexCoords);
}