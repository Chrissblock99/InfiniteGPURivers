package me.chriss99;

import java.io.File;

import static me.chriss99.Main.loadShader;
import static me.chriss99.Main.printErrors;
import static org.lwjgl.opengl.GL45.*;

public class ComputeProgram {
    final int program;
    final int shader;

    public ComputeProgram(String name) {
        shader = loadShader(new File("/home/chriss99/IdeaProjects/ogl_test2/src/main/java/me/chriss99/" + name + ".comp"), GL_COMPUTE_SHADER);
        program = glCreateProgram();

        glAttachShader(program, shader);
        glLinkProgram(program);
        glValidateProgram(program);


        int compile =  glGetShaderi(shader,   GL_COMPILE_STATUS);
        int link =     glGetProgrami(program, GL_LINK_STATUS);
        int validate = glGetProgrami(program, GL_VALIDATE_STATUS);

        if (compile == 1 && link == 1 && validate == 1)
            return;

        System.out.println("Stats for compute shader: " + name);
        System.out.println("Compute Shader Compiled: "  + compile);
        System.out.println("Program Linked: " 			+ link);
        System.out.println("Program Validated: " 		+ validate);
        printErrors();
    }

    public void delete() {
        glUseProgram(program);
        glDetachShader(program, shader);
        glDeleteShader(shader);

        glUseProgram(0);
        glDeleteProgram(program);

        printErrors();
    }
}
