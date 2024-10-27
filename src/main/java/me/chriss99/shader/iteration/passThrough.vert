#version 450 core

uniform ivec2 srcPos;
uniform int width;

in float height;

void main(void) {
    int i = gl_VertexID;

    int x = i % width;
    int z = (i - x) / width;

    gl_Position = vec4((x + srcPos.x)*64, height, (z + srcPos.y)*64, 1);
}