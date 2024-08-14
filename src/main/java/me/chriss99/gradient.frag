#version 450 core

in vec3 pos;

out vec4 FragColor;

void main(void) {
    float gradient = pos.y/100 + .5;
    FragColor = vec4(
    gradient*.7+.3,
    gradient*.8+.2,
    (int(pos.x)%2==0) ? .33 : 0 + ((int(pos.z)%2==0) ? .33 : 0),
    1.0);
}