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

    gl_Position = vec4(x*scale + srcPos.x, water ? position.y : position.x, z*scale + srcPos.y, 1);
    oHeight = water ? position.x : position.y;
}