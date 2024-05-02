#version 450 core

//position is bound to attribute index 0 and color is bound to attribute index 1
in  vec2 position;
in  vec3 color;

//this vector is updated each frame to animate the triangle
uniform vec2 positionMod = vec2(1,1);

//output the outColor variable to the next shader in the chain
out vec3 outColor;

void main(void) {

    //syntax: vec4(x, y, z, w);
    gl_Position = vec4(position.x * positionMod.x, position.y * positionMod.y, 0.0, 1.0);

    //pass the output color right to the fragment shader without changing it
    outColor = color;
}