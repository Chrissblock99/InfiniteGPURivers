#version 450 core

uniform vec3 cameraPos;

in vec3 pos;
in vec3 normal;

out vec4 FragColor;

const vec3 lightDir = normalize(vec3(1, -1, 1));

void main(void) {
    vec3 color = vec3(0.375);


    float diffuse = dot(normal, lightDir);
    diffuse = diffuse*.5+.5;


    FragColor = vec4(color * diffuse, .75);
}