#type vertex
#version 430 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aTexID;
layout (location=4) in mat4 aTransform;

uniform mat4 uProjection;
uniform mat4 uView;

out vec4 fColor;
out vec2 fTexCoords;
flat out uint fTexID;

void main()
{
    fColor = aColor;
    fTexCoords = aTexCoords;
    //fTexID = int(aTexID);
    fTexID = uint(aTexID);

    gl_Position = (uProjection * uView * aTransform) * vec4(aPos, 1.0);
}

#type fragment
#version 430 core

struct Material {
	vec4 ambientColor;
};

layout(std430, binding = 0) readonly buffer materialBuffer
{
    Material materials[];
};

in vec4 fColor;
in vec2 fTexCoords;
flat in uint fTexID;

uniform sampler2D uTextures[32];

out vec4 color;

void main()
{
	vec4 ambient = materials[0].ambientColor;
    //color = fColor * texture(uTextures[fTexID], fTexCoords);
    //color = fColor * texture(uTextures[fTexID], fTexCoords) + ambient;
    color = ambient;
}