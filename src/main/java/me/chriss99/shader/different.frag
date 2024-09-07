#version 450 core

uniform bool water;

in vec3 pos;
in vec3 normal;

out vec4 FragColor;

void main(void) {
    vec3 color = water ? vec3(.2, .24, .6) : vec3(.8, .76, .4);

    float exposedness = dot(normal, normalize(vec3(1, -1, 1)));
    exposedness = exposedness*.5+.5;

    FragColor = vec4(color * exposedness, 1.0);
}