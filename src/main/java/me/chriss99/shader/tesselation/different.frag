#version 450 core

uniform vec3 cameraPos;
uniform bool water;

in vec3 pos;
in vec3 normal;

out vec4 FragColor;

const vec3 lightDir = normalize(vec3(1, -1, 1));

void main(void) {
    float angle0to1 = acos(normal.y)/3.1415926535*.5;
    vec3 color = water ? vec3(.2, .24, .6) : ((angle0to1 < 0.4) ? vec3(0.375) : vec3(.1875, .375, .0625));


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