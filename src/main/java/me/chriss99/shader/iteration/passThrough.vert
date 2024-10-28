#version 450 core

uniform ivec2 srcPos;
uniform int width;

in vec3 position;

void main(void) {
    gl_Position = vec4(position, 1);
}