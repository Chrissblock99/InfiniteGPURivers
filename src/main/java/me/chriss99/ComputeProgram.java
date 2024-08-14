package me.chriss99;

import static org.lwjgl.opengl.GL45.*;

public class ComputeProgram extends GLProgram {
    public ComputeProgram(String name) {
        addShader( name + ".comp", GL_COMPUTE_SHADER);
        validate();
    }
}
