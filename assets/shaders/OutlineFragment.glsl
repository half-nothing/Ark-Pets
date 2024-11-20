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

vec4[8] getNeighbors(vec2 unitLength) {
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

vec4 getOutlined() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    if (u_outlineWidth > 0.0) {
        vec2 relOutlineWidth = vec2(1.0) / u_textureSize * u_outlineWidth;
        vec4[8] neighbors = getNeighbors(relOutlineWidth);
        float neighborAlpha = 0.0;
        for (int i = 0; i < neighbors.length(); i++) {
            neighborAlpha += neighbors[i].a;
        }
        if (neighborAlpha > c_alphaLv0) {
            texColor.rgb = u_outlineColor.rgb;
            texColor.a = min(1.0, neighborAlpha) * u_outlineColor.a;
        }
    }
    return texColor;
}

vec4 getSeamed() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    vec2 relPixelSize = vec2(1.0) / u_textureSize;
    vec4[8] neighbors = getNeighbors(relPixelSize);
    vec4 sampleColor = vec4(0.0);
    int sampleSize = 0;
    for (int i = 0; i < neighbors.length(); i++) {
        if (neighbors[i].a > c_alphaLv2) {
            sampleColor += neighbors[i];
            sampleSize += 1;
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
    ivec2 texSize = u_textureSize;

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
