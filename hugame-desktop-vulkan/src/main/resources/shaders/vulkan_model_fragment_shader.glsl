#version 450
#extension GL_EXT_nonuniform_qualifier : require

struct Material {
    vec3 ambient;
    int albedoMapTextureIndex;

    vec3 diffuse;
    int normalMapTextureIndex;

    vec3 specular;
    int specularMapTextureIndex;

    float shininess;
    int albedoMapTextureLayer;
    int normalMapTextureLayer;
    int specularMapTextureLayer;
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

const float ambientStrength = 0.5;
const float diffuseStrength = 0.5;
const float specularStrength = 0.3;

layout(binding = 1) uniform UniformBuffer {
    vec3 cameraPosition;
    int pointLightAmount;
    int spotLightAmount;
    int directionalLightAmount;
} uniformBuffer;

layout(binding = 2) uniform sampler2DArray textures[32];

layout (std430, binding = 3) readonly buffer materialBuffer
{
    Material materials[];
};

layout (std430, binding = 4) readonly buffer pointLightBuffer
{
    PointLight pointLights[];
};

layout (std430, binding = 5) readonly buffer spotLightBuffer
{
    SpotLight spotLights[];
};

layout (std430, binding = 6) readonly buffer directionalLightBuffer
{
    DirectionalLight directionalLights[];
};

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 textureCoordinates;
layout(location = 3) flat in int materialIndex;

//layout(location = 0) in vec3 fragColor;
//layout(location = 1) in vec2 fragTextureCoordinates;
//layout(location = 2) flat in int fragTextureIndex;

layout(location = 0) out vec4 color;

vec3 calculatePointLight(PointLight light, Material material);
vec3 calculateSpotLight(SpotLight light, Material material);
vec3 calculateDirectionalLight(DirectionalLight light, Material material);

// TODO: Can remove?
float getAttenuation(vec3 lightPos, vec3 fragPos, float constant, float linear, float quadratic);

//void main() {
//    vec4 textureSample = texture(textureSampler[nonuniformEXT(fragTextureIndex)], fragTextureCoordinates);
//    color = textureSample;
//}

void main() {
    Material material = materials[materialIndex];
    vec3 light = vec3(0.0, 0.0, 0.0);

    for (int i = 0; i < uniformBuffer.pointLightAmount; i++) {
        light += calculatePointLight(pointLights[i], material);
    }

    for (int i = 0; i < uniformBuffer.spotLightAmount; i++) {
        light += calculateSpotLight(spotLights[i], material);
    }

    for (int i = 0; i < uniformBuffer.directionalLightAmount; i++) {
        light += calculateDirectionalLight(directionalLights[i], material);
    }
    //float l = 10 / (length(uniformBuffer.cameraPosition - position));
    //light = vec3(l);
    //light = normal;

    vec4 textureSample;
    int albedoMapTextureIndex = material.albedoMapTextureIndex;
    int albedoMapTextureLayer = material.albedoMapTextureLayer;
    if (albedoMapTextureIndex >= 0) {
        textureSample = texture(textures[albedoMapTextureIndex], vec3(textureCoordinates, albedoMapTextureLayer));
    } else {
        textureSample = vec4(1, 1, 1, 1);
    }

    color = textureSample * vec4(light, 1.0);
    //color = vec4(light, 1.0);
    //color = vec4(light, 1.0);
    //color = textureSample;
}

vec3 calculatePointLight(PointLight light, Material material) {
    float distance = length(light.position - position);
    float attenuation = 1.0 / (light.constant + light.linear * distance +
    light.quadratic * (distance * distance));

    vec3 ambient = light.color * attenuation * material.ambient;

    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(light.position - position);

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = light.color * diff * attenuation * material.diffuse;

    vec3 viewDir = normalize((uniformBuffer.cameraPosition) - position);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = light.color * spec * specularStrength * attenuation * material.specular;

    return (ambient + diffuse + specular) * light.strength;
}

vec3 calculateSpotLight(SpotLight light, Material material) {
    vec3 lightDir = normalize(light.position - position); // Frag to light
    vec3 viewDir = normalize(uniformBuffer.cameraPosition - position);
    vec3 norm = normalize(normal);
    float distance = length(light.position - position);
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

    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(-light.direction); // Frag to light

    vec3 diffuse = max(dot(norm, lightDir), 0.0) * light.color * material.diffuse;

    vec3 viewDir = normalize(uniformBuffer.cameraPosition - position); // Frag to camera
    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0) * light.color * material.specular;

    return (ambient + diffuse + specular) * light.strength;
}