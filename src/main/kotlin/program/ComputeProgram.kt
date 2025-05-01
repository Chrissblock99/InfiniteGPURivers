package me.chriss99.program;

import static org.lwjgl.opengl.GL45.*;

public class ComputeProgram extends GLProgram {
    public ComputeProgram(String name) {
        addShader( "compute/" + name + ".comp", GL_COMPUTE_SHADER);
        validate();
    }
}
