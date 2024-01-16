#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexArrayID;
in float fTexArrayIndex;

uniform sampler2DArray uTextures[32];

out vec4 color;

void main()
{
	int id = int(fTexArrayID);
    color = fColor * texture(uTextures[id], vec3(fTexCoords, fTexArrayIndex));
}