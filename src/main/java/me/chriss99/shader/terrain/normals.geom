#version 450 core
layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

uniform mat4 transformMatrix;

out vec3 pos;
out vec3 normal;

void main() {
    vec3 constNormal = normalize(cross(gl_in[1].gl_Position.xyz-gl_in[0].gl_Position.xyz, gl_in[2].gl_Position.xyz-gl_in[0].gl_Position.xyz));

    for (int i = 0; i < 3; i++) {
        pos = gl_in[i].gl_Position.xyz;
        gl_Position = transformMatrix * vec4(pos, 1);
        normal = constNormal;
        EmitVertex();
    }
    EndPrimitive();
}