#version 450 core

uniform bool water;

in vec4 position;

void main(void) {
    gl_Position = vec4(water ? position.xwy : position.xzy, 1);
}