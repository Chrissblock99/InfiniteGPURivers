#version 450 core

uniform vec3 cameraPos;
uniform bool water;

in vec3 pos;
in vec3 normal;

out vec4 FragColor;

const vec3 lightDir = normalize(vec3(1, -1, 1));

void main(void) {
    vec3 color = water ? vec3(.2, .24, .6) : vec3(.8, .76, .4);


    float diffuse = dot(normal, lightDir);
    diffuse = diffuse*.5+.5;


    float specular = 0;
    if (water) {
        vec3 viewDir = normalize(cameraPos - pos);
        vec3 reflectDir = reflect(lightDir, normal);
        specular = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    }


    FragColor = vec4(color * (diffuse + specular), water ? 0.6 : 1.0);
}