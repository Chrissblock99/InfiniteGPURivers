#version 450 core
layout (quads, equal_spacing, ccw) in;
layout(binding = 0, r32f) restrict readonly uniform image2D bedrockMap;
layout(binding = 1, r32f) restrict readonly uniform image2D streamMap;

uniform bool water;
uniform ivec2 srcPos;

out float oHeight;

void main() {
    vec2 uv = gl_TessCoord.xy;

    vec2 p00 = gl_in[0].gl_Position.xz;
    vec2 p11 = gl_in[3].gl_Position.xz;

    vec2 position = (p11 - p00) * uv + p00;
    ivec2 texPosition = ivec2(position)+1;

    float height = imageLoad(bedrockMap, texPosition).x;
    float waterHeight = imageLoad(streamMap, texPosition).x;

    vec2 heights = (vec2(0, max(-1, sqrt(waterHeight) - sqrt(1176.46*20))) + height)/1176.46;
    if (water)
        heights = heights.yx;

    gl_Position =  vec4(position.x + srcPos.x, heights.x, position.y + srcPos.y, 1);
    oHeight = heights.y;
}