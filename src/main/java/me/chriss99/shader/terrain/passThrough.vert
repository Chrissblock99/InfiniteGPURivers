#version 450 core

uniform bool water;
uniform ivec2 srcPos;
uniform int width;

in vec2 position;

void main(void) {
    int i = gl_VertexID;

    int x = i % width;
    int z = (i - x) / width;

    gl_Position = vec4(x + srcPos.x, water ? position.y : position.x, z + srcPos.y, 1);
}