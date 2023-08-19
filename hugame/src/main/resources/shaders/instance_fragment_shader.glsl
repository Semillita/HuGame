#version 430 core

struct Material {
    vec3 ambient;
    int albedoMapID;

    vec3 diffuse;
    int normalMapID;

    vec3 specular;
    int specularMapID;

    float shininess;
    int albedoMapSlice;
    int normalMapSlice;
    int specularMapSlice;
};

struct PointLight {
    vec3 position;
    float constant;

    vec3 color;
    float linear;

    float quadratic;
    float strength;
    float bullshit0;
    float bullshit1;
};

struct SpotLight {
    vec3 position;
    float constant;

    vec3 direction;
    float linear;

    vec3 color;
    float quadratic;

    float strength;
    float cutOff;
    float outerCutOff;
    float bullshit;
};

struct DirectionalLight {
    vec3 direction;
    float strength;

    vec3 color;
    float bullshit;
};

layout (std430, binding = 0) readonly buffer materialBuffer
{
    Material materials[];
};

layout (std430, binding = 1) readonly buffer pointLightBuffer
{
    PointLight pointLights[];
};

layout (std430, binding = 2) readonly buffer spotLightBuffer
{
    SpotLight spotLights[];
};

layout (std430, binding = 3) readonly buffer directionalLightBuffer
{
    DirectionalLight directionalLights[];
};

in vec3 fPosition;
in vec3 fNormal;
in vec2 fTexCoords;
flat in int fMatID;

uniform vec3 cameraPosition;
uniform sampler2DArray uTextures[32];
uniform int pointLightAmount;
uniform int spotLightAmount;
uniform int directionalLightAmount;

out vec4 color;

const float ambientStrength = 0.5;
const float diffuseStrength = 0.5;
const float specularStrength = 0.3;

vec3 calculatePointLight(PointLight light, Material material);
vec3 calculateSpotLight(SpotLight light, Material material);
vec3 calculateDirectionalLight(DirectionalLight light, Material material);

float getAttenuation(vec3 lightPos, vec3 fragPos, float constant, float linear, float quadratic);

void main()
{
    Material material = materials[fMatID];
    vec3 light = vec3(0.0, 0.0, 0.0);

    for (int i = 0; i < pointLightAmount; i++) {
        light += calculatePointLight(pointLights[i], material);
    }

    for (int i = 0; i < spotLightAmount; i++) {
        light += calculateSpotLight(spotLights[i], material);
    }

    for (int i = 0; i < directionalLightAmount; i++) {
        light += calculateDirectionalLight(directionalLights[i], material);
    }

    vec4 textureSample;
    int albedoID = material.albedoMapID;
    if (albedoID >= 0) {
        textureSample = texture(uTextures[albedoID], vec3(fTexCoords, material.albedoMapSlice));
    } else {
        textureSample = vec4(1, 1, 1, 1);
    }

    color = textureSample * vec4(light, 1.0);

    //float c = albedoID / 5.0;
    //color = vec4(c, c, c, 1);
}

vec3 calculatePointLight(PointLight light, Material material) {
    float distance = length(light.position - fPosition);
    float attenuation = 1.0 / (light.constant + light.linear * distance +
    light.quadratic * (distance * distance));

    vec3 ambient = light.color * attenuation * material.ambient;

    vec3 norm = normalize(fNormal);
    vec3 lightDir = normalize(light.position - fPosition);

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = light.color * diff * attenuation * material.diffuse;

    vec3 viewDir = normalize(cameraPosition - fPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = light.color * spec * specularStrength * attenuation * material.specular;

    return (ambient + diffuse + specular) * light.strength;
}

vec3 calculateSpotLight(SpotLight light, Material material) {
    vec3 lightDir = normalize(light.position - fPosition); // Frag to light
    vec3 viewDir = normalize(cameraPosition - fPosition);
    vec3 norm = normalize(fNormal);
    float distance = length(light.position - fPosition);
    float attenuation = light.strength /
    (light.constant + light.linear * distance + light.quadratic * (distance * distance));

    float theta = dot(lightDir, normalize(-light.direction)); // Cosine of angle between spot dir and light dir

    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);

    vec3 diffuse = max(dot(norm, lightDir), 0.0) * light.color * intensity * material.diffuse;

    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0) * light.color * intensity * material.specular;

    return (diffuse + specular) * attenuation;
}

vec3 calculateDirectionalLight(DirectionalLight light, Material material) {
    vec3 ambient = light.color * 0.05 * material.ambient;

    vec3 norm = normalize(fNormal);
    vec3 lightDir = normalize(-light.direction); // Frag to light

    vec3 diffuse = max(dot(norm, lightDir), 0.0) * light.color * material.diffuse;

    vec3 viewDir = normalize(cameraPosition - fPosition); // Frag to camera
    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0) * light.color * material.specular;

    return (ambient + diffuse + specular) * light.strength;
}