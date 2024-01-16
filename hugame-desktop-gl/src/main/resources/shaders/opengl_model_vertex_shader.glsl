#version 430 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec3 aNormal;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aMatID;
layout (location=4) in mat4 aTransform;

uniform mat4 uProjection;
uniform mat4 uView;

out vec3 fPosition;
out vec3 fNormal;
out vec2 fTexCoords;
flat out int fMatID;

void main()
{
    fNormal = aNormal; // TODO: normal matrix
    fTexCoords = aTexCoords;
    fMatID = int(aMatID);

	gl_Position = (uProjection * uView * aTransform) * vec4(aPos, 1.0);
    fPosition = vec3(aTransform * vec4(aPos, 1.0));
}