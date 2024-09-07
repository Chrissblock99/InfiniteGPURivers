package me.chriss99;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static me.chriss99.Main.printErrors;
import static org.lwjgl.opengl.GL45.*;

public class GLProgram {
    public final int program;
    private final ArrayList<Integer> shaders = new ArrayList<>();

    public GLProgram() {
        this.program = glCreateProgram();
    }

    public void addShader(String name, int type) {
        int shader = loadShader(new File("src/main/java/me/chriss99/shader/" + name), type);
        if (glGetShaderi(shader,  GL_COMPILE_STATUS) != 1)
            System.out.println("Shader " + name + " did not compile!");

        shaders.add(shader);
        glAttachShader(program, shader);
    }

    public void bindAttribute(int index, String name) {
        glBindAttribLocation(program, index, name);
    }

    public void validate() {
        glLinkProgram(program);
        glValidateProgram(program);

        int link =     glGetProgrami(program, GL_LINK_STATUS);
        int validate = glGetProgrami(program, GL_VALIDATE_STATUS);

        if (link == 1 && validate == 1)
            return;

        System.out.println("Program Linked: "    + link);
        System.out.println("Program Validated: " + validate);
        printErrors();
    }

    public void use() {
        glUseProgram(program);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(program, name);
    }

    public void delete() {
        //detach the shaders from the program object
        for (Integer shader : shaders) {
            glDetachShader(program, shader);
            glDeleteShader(shader);
        }

        //stop using the shader program
        glUseProgram(0);

        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(program);
    }

    public static int loadShader(File file, int type) {
        try {
            Scanner sc = new Scanner(file);
            StringBuilder data = new StringBuilder();

            if(file.exists()) {
                while(sc.hasNextLine()) {
                    data.append(sc.nextLine()).append("\n");
                }

                sc.close();
            }
            int id = glCreateShader(type);
            glShaderSource(id, data);
            glCompileShader(id);
            return id;
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
