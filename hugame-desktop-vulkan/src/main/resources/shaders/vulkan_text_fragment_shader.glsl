#version 450

layout(binding = 1) uniform UniformBuffer {
    float pxRange;
} uniformBuffer;

layout(binding = 2) uniform sampler2DArray[32] textures;

layout(location = 0) in vec4 textColor;
layout(location = 1) in vec2 textureCoordinates;
layout(location = 2) flat in int textureIndex;
layout(location = 3) flat in int textureLayer;

layout(location = 0) out vec4 color;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    // Use the below method for 3D
    /*vec2 unitRange = vec2(pxRange)/vec2(textureSize(textures[textureIndex], 0));
    vec2 screenTexSize = vec2(1.0)/fwidth(textureCoordinates);
    return max(0.5*dot(unitRange, screenTexSize), 1.0);*/

    return uniformBuffer.pxRange;
    //return 0.125;
}

void main() {
    vec3 msd = texture(textures[textureIndex], vec3(textureCoordinates, textureLayer)).rgb;
    float sd = median(msd.r, msd.g, msd.b); // 0 - 1
    float screenPxDistance = screenPxRange()*(sd - 0.5); // (sd - 0.5) is -0.5 - 0.5, spd is -0.5*0.125 - 0.5*0.125
    float opacity = clamp(screenPxDistance + 0.5, 0.0, 1.0);

    //color = vec4(sd, sd, sd, 1);

    // Try clamping the color:
    //sd = clamp(sd, 0.0, 1.0);

    float sd2 = sd - 0.5;
    //color = vec4(sd2, sd2, sd2, 1);

    float spd = screenPxRange() * sd2;
    //color = vec4(spd, spd, spd, 1);

    float spd2 = spd + 0.5;
    //color = vec4(spd2, spd2, spd2, 1);

    //float op = clamp(spd2, 0.0, 1.0);
    float op = opacity;
    color = vec4(op, op, op, 1);

    //vec3 textureSample = texture(textures[textureIndex], vec3(textureCoordinates, textureLayer)).rgb;
    //color = vec4(textureSample, 1.0);
    //float r = 1 / textureCoordinates.x;
    //float b = 1 / textureCoordinates.y;
    //color = vec4(r, 0, b, 1);
    //color = vec4(vec3(1, 1, 1), op) + vec4(1, 1, 1, 0.1);
    //color = texture(textures[textureIndex], vec3(textureCoordinates, textureLayer));
}