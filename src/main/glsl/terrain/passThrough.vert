#version 450 core

uniform bool water;
uniform ivec2 srcPos;
uniform int width;
uniform int scale;

in vec2 position;

out float oHeight;

void main(void) {
    int i = gl_VertexID;

    int x = i % width;
    int z = (i - x) / width;

    vec2 heights = (vec2(0, max(-1, sqrt(position.y) - sqrt(1176.46*20))) + position.x)/1176.46;
    if (heights.x == 0)
        heights.x = -10;

    if (water)
        heights = heights.yx;

    gl_Position = vec4(x*scale + srcPos.x, heights.x, z*scale + srcPos.y, 1);
    oHeight = heights.y;
}