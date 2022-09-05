#version 430 core

struct Material {
	vec4 ambientColor;
};

struct PointLight {
    vec3 position;
    float constant;
  
    vec3 ambient;
    float linear;
    
    vec3 diffuse;
    float quadratic;
    
    vec3 specular;
    float bullshit;
};

layout(std430, binding = 0) readonly buffer materialBuffer
{
    Material materials[];
};

layout(std430, binding = 1) readonly buffer pointLightBuffer
{
	PointLight pointLights[];
};

in vec3 fPosition;
in vec3 fNormal;
in vec2 fTexCoords;
flat in uint fTexID;
flat in uint fMaterialIndex;

uniform vec3 cameraPosition;
uniform sampler2D uTextures[32];
uniform int pointLightAmount;

out vec4 color;

const float ambientStrength = 1.0;
const float diffuseStrength = 1.0;
const float specularStrength = 0.3;

vec3 calculatePointLight(PointLight light);

void main()
{

	vec3 light = vec3(0.0, 0.0, 0.0);
	
	for (int i = 0; i < pointLightAmount; i++) {
		light += calculatePointLight(pointLights[i]);
	}
	
	vec4 ambient = materials[fMaterialIndex].ambientColor;
    color = texture(uTextures[fTexID], fTexCoords) * vec4(light.xyz, 1.0) * ambient;
}

vec3 calculatePointLight(PointLight light) {
	float distance = length(light.position - fPosition);
	float attenuation = 1.0 / (light.constant + light.linear * distance + 
    		    light.quadratic * (distance * distance));
    		    
    vec3 ambient = light.ambient * ambientStrength * attenuation;
    
    vec3 norm = normalize(fNormal);
    vec3 lightDir = normalize(light.position - fPosition);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = light.diffuse * diff * diffuseStrength * attenuation;
    
    vec3 viewDir = normalize(cameraPosition - fPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = light.specular * spec * specularStrength * attenuation;
    
    return ambient + diffuse + specular;
}