#version 450 core
layout (quads, equal_spacing, ccw) in;
layout(binding = 0, r32f) restrict readonly uniform image2D terrainMap;
layout(binding = 1, r32f) restrict readonly uniform image2D waterMap;

uniform bool water;

void main() {
    vec2 uv = gl_TessCoord.xy;

    vec2 p00 = gl_in[0].gl_Position.xz;
    vec2 p11 = gl_in[3].gl_Position.xz;

    vec2 position = (p11 - p00) * uv + p00;


    float height = imageLoad(terrainMap, ivec2(position)).x;
    if (water)
        height += imageLoad(waterMap, ivec2(position)).x - .03;

    gl_Position =  vec4(position.x, height, position.y, 1);
}