#version 430 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aNormal;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aTexID;
layout (location=4) in mat4 aTransform;
layout (location=8) in int aMaterialIndex;

uniform mat4 uProjection;
uniform mat4 uView;

out vec4 fNormal;
out vec2 fTexCoords;
flat out uint fTexID;
out int fMaterialIndex;

void main()
{
    fNormal = aNormal;
    fTexCoords = aTexCoords;
    fTexID = uint(aTexID);
    fMaterialIndex = aMaterialIndex;

    gl_Position = (uProjection * uView * aTransform) * vec4(aPos, 1.0);
}