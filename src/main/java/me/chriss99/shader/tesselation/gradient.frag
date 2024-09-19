#version 450 core

uniform bool water;

in vec3 pos;

out vec4 FragColor;

void main(void) {
    float gradient = pos.y/100 + .5;
    vec3 color;

    if (!water)
        color = vec3(
            gradient*.7+.3,
            gradient*.8+.2,
            (int(pos.x)%2==0) ? .33 : 0 + ((int(pos.z)%2==0) ? .33 : 0));
    else
        color = vec3(
            0,
            (int(pos.x)%2==0) ? .33 : 0 + ((int(pos.z)%2==0) ? .33 : 0),
            gradient);

    FragColor = vec4(color, 1.0);
}