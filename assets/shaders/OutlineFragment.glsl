/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */

// Gap Seaming and Ouline Effect Fragment Shader for TwoColorPolygonBatch.

#version 120

varying vec2 v_texCoords;       // From VS
uniform sampler2D u_texture;    // From TCPB
uniform vec4 u_outlineColor;    // Required
uniform float u_outlineWidth;   // Required
uniform ivec2 u_textureSize;    // Required
uniform float u_alpha;          // Required

const float c_alphaLv0 = 0.1;
const float c_alphaLv1 = 0.5;
const float c_alphaLv2 = 0.9;
const float c_seamCoef = 0.5;
const float c_outlineOverstate = 10.0;

vec4[8] getSimpleNeighbors(vec2 unitLength) {
    vec4[8] result;
    result[0] = texture2D(u_texture, v_texCoords + vec2(unitLength.x, 0.0));
    result[1] = texture2D(u_texture, v_texCoords - vec2(unitLength.x, 0.0));
    result[2] = texture2D(u_texture, v_texCoords + vec2(0.0, unitLength.y));
    result[3] = texture2D(u_texture, v_texCoords - vec2(0.0, unitLength.y));
    result[4] = texture2D(u_texture, v_texCoords + unitLength);
    result[5] = texture2D(u_texture, v_texCoords - unitLength);
    result[6] = texture2D(u_texture, v_texCoords + vec2(unitLength.x, -unitLength.y));
    result[7] = texture2D(u_texture, v_texCoords - vec2(unitLength.x, -unitLength.y));
    return result;
}

vec4[24] getGaussianNeighbors(vec2 unitLength) {
    float kernel[25] = float[25] (
        0.0035434, 0.0158805, 0.0261825, 0.0158805, 0.0035434,
        0.0158805, 0.0711714, 0.1173418, 0.0711714, 0.0158805,
        0.0261825, 0.1173418, 0.0      , 0.1173418, 0.0261825,
        0.0158805, 0.0711714, 0.1173418, 0.0711714, 0.0158805,
        0.0035434, 0.0158805, 0.0261825, 0.0158805, 0.0035434
    );
    vec4 neighbors[24];
    int i = 0;
    for (int y = -2; y <= 2; y++) {
        for (int x = -2; x <= 2; x++) {
            if (y != 0 && x != 0) {
                vec2 offset = vec2(x, y) * unitLength;
                neighbors[i] = texture2D(u_texture, v_texCoords + offset) * kernel[i];
                i++;
            }
        }
    }
    return neighbors;
}

vec4 getGaussianNeighborsSum(vec2 unitLength) {
    vec4[24] neighbors = getGaussianNeighbors(unitLength);
    vec4 sum = vec4(0.0);
    for (int i = 0; i < neighbors.length(); i++) {
        sum += neighbors[i];
    }
    return sum;
}

vec4 getOutlined() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    if (u_outlineWidth > 0.0) {
        vec2 relOutlineWidth = vec2(1.0) / u_textureSize * u_outlineWidth;
        vec4 neighbor = getGaussianNeighborsSum(relOutlineWidth) * c_outlineOverstate;
        if (neighbor.a > c_alphaLv0) {
            texColor.rgb = u_outlineColor.rgb;
            texColor.a = min(1.0, neighbor.a) * u_outlineColor.a;
        }
    }
    return texColor;
}

vec4 getSeamed() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    vec2 relPixelSize = vec2(1.0) / u_textureSize;
    vec4[8] neighbors = getSimpleNeighbors(relPixelSize);
    vec4 sampleColor = vec4(0.0);
    int sampleSize = 0;
    for (int i = 0; i < neighbors.length(); i++) {
        if (neighbors[i].a > c_alphaLv2) {
            sampleColor += neighbors[i];
            sampleSize++;
        }
    }
    if (sampleSize > 0) {
        texColor.rgb = sampleColor.rgb / sampleSize * c_seamCoef + texColor.rgb * (1.0 - c_seamCoef);
        texColor.a = sampleColor.a / sampleSize;
    }
    return texColor;
}

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);

    if (texColor.a < c_alphaLv0) {
        // Outline effect apply on transparent areas
        texColor = getOutlined();
    } else if (texColor.a < c_alphaLv1) {
        // No effect apply on these areas
    } else if (texColor.a < c_alphaLv2) {
        // Seaming apply on gap areas
        texColor = getSeamed();
    } else {
        // No effect apply on other areas
    }

    // Ultimate composing
    gl_FragColor = texColor;
    gl_FragColor.a *= u_alpha;
}
