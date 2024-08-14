#version 450 core

in vec2 position;

void main(void) {
    gl_Position = vec4(position.x, 0, position.y, 1);
}