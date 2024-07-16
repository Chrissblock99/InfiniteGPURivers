package me.chriss99;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;

public class Texture {
    final int texture;
    int location;

    final int internalFormat;
    final int width;
    final int height;

    public Texture(int internalFormat, int width, int height) {
        this.internalFormat = internalFormat;
        this.width = width;
        this.height = height;

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexStorage2D(GL_TEXTURE_2D, 1, internalFormat, width, height);
    }

    public void bindUniformImage(int program, int bindingUnit, String name, int access) {
        glUseProgram(program);

        glBindImageTexture(bindingUnit, texture, 0, false, 0, access, internalFormat);
        location = glGetUniformLocation(program, name);
        glUniform1i(location, bindingUnit);
    }

    public void uploadData(int xOffset, int yOffset, int width, int height, int format, int type, ByteBuffer data) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexSubImage2D(GL_TEXTURE_2D, 0, xOffset, yOffset, width, height, format, type, data);
    }

    public void uploadFullData(int format, int type, ByteBuffer data) {
        uploadData(0, 0, width, height, format, type, data);
    }

    public void downloadData(int format, int type, ByteBuffer buffer) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glGetTexImage(GL_TEXTURE_2D, 0, format, type, buffer);
    }
}
