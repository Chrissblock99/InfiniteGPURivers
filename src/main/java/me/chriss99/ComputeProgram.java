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

        System.out.println("Stats for compute shader: " + name);
        System.out.println("Compute Shader Compiled: "   	+ glGetShaderi(shader, 	GL_COMPILE_STATUS));
        System.out.println("Program Linked: " 				+ glGetProgrami(program, 		GL_LINK_STATUS));
        System.out.println("Program Validated: " 			+ glGetProgrami(program, 		GL_VALIDATE_STATUS));
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
