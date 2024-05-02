#version 450 core
// It was expressed that some drivers required this next line to function properly
//precision highp float;

//use the outColor variable from the vertex shader as the input to the fragment shader
in vec3 outColor;

//this is updated every frame in the main loop
uniform vec4 colorMod;

//the built in pixel color variable
out vec4 FragColor;

void main(void) {
    // Pass through our original color with full opacity.
    FragColor = vec4(outColor.r * colorMod.r, outColor.g * colorMod.g, outColor.b * colorMod.b, 1.0 * colorMod.a);
}