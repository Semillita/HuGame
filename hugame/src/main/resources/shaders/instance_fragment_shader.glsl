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
    float strength;
};

struct SpotLight {
    vec3 position;
    float constant;
  
    vec3 direction;
    float linear;
    
    vec3 ambient;
    float quadratic;
    
    vec3 diffuse;
    float strength;
    
    vec3 specular;
    float angle;
};

struct DirectionalLight {
    vec3 direction;
    float strength;
  
    vec3 ambient;
    float bullshit1;
    
    vec3 diffuse;
    float bullshit2;
    
    vec3 specular;
    float bullshit3;
};

layout(std430, binding = 0) readonly buffer materialBuffer
{
    Material materials[];
};

layout(std430, binding = 1) readonly buffer pointLightBuffer
{
	PointLight pointLights[];
};

layout(std430, binding = 2) readonly buffer spotLightBuffer
{
	SpotLight spotLights[];
};

layout(std430, binding = 3) readonly buffer directionalLightBuffer
{
	DirectionalLight directionalLights[];
};

in vec3 fPosition;
in vec3 fNormal;
in vec2 fTexCoords;
flat in uint fTexID;
flat in uint fMaterialIndex;

uniform vec3 cameraPosition;
uniform sampler2D uTextures[32];
uniform int pointLightAmount;
uniform int spotLightAmount;
uniform int directionalLightAmount;

out vec4 color;

const float ambientStrength = 1.0;
const float diffuseStrength = 1.0;
const float specularStrength = 0.3;

vec3 calculatePointLight(PointLight light);
vec3 calculateSpotLight(SpotLight light);
vec3 calculateDirectionalLight(DirectionalLight light);

void main()
{

	//vec3 light = vec3(0.0, 0.0, 0.0);
	
	//for (int i = 0; i < pointLightAmount; i++) {
	//	light += calculatePointLight(pointLights[i]);
	//}
	
	//for (int i = 0; i < spotLightAmount; i++) {
	//	light += calculateSpotLight(spotLights[i]);
	//}
	
	//for (int i = 0; i < directionalLightAmount; i++) {
	//	light += calculateDirectionalLight(directionalLights[i]);
	//}
	
	//vec4 ambient = materials[fMaterialIndex].ambientColor;
    //color = texture(uTextures[fTexID], fTexCoords) * vec4(light.xyz, 1.0) * ambient;
    //color = vec4(light, 1.0);
    color = vec4(calculateSpotLight(spotLights[0]), 1.0);
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
    
    return (ambient + diffuse + specular) * light.strength;
}

vec3 calculateSpotLight(SpotLight light) {
    vec3 lightDir = normalize(light.direction);
    float theta = dot(lightDir, normalize(-light.direction));
    
    if (theta < 0) {
    	return vec3(0, 1, 0);
    } else {
    	return vec3(theta * 100);
    }
    
    return vec3(1);
    
    //return vec3(theta);
    
    /*if (theta < light.angle) {
    	return vec3(0);
    }
    		    
    vec3 ambient = light.ambient * ambientStrength;
    
    vec3 norm = normalize(fNormal);
    float diffFactor = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = light.diffuse * diffFactor * diffuseStrength;
    
    vec3 viewDir = normalize(cameraPosition - fPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float specFactor = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = light.specular * specFactor * specularStrength;
    
    //return (ambient + diffuse + specular) * light.strength;
    return vec3(0.5, 0.5, 0.5);*/
}

vec3 calculateDirectionalLight(DirectionalLight light) {
	//float distance = length(light.position - fPosition);
	//float attenuation = 1.0 / (light.constant + light.linear * distance + 
    //		    light.quadratic * (distance * distance));
    		    
    vec3 ambient = light.ambient * ambientStrength;
    
    vec3 norm = normalize(fNormal);
    vec3 lightDir = normalize(light.direction);
    float diffFactor = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = light.diffuse * diffFactor * diffuseStrength;
    
    vec3 viewDir = normalize(cameraPosition - fPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float specFactor = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = light.specular * specFactor * specularStrength;
    
    return (ambient + diffuse + specular) * light.strength;
}