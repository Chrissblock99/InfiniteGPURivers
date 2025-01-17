package me.chriss99;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;

public class Texture2D {
    final int texture;

    final int internalFormat;
    final int width;
    final int height;

    public Texture2D(int internalFormat, int width, int height) {
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
        int location = glGetUniformLocation(program, name);
        glUniform1i(location, bindingUnit);
    }

    public void uploadData(int xOffset, int yOffset, Array2DBufferWrapper data) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexSubImage2D(GL_TEXTURE_2D, 0, xOffset, yOffset, data.width, data.height, data.type.glFormat, data.type.glType, data.buffer);
    }

    public void downloadData(int xOffset, int yOffset, Array2DBufferWrapper writeTo) {
        glBindTexture(GL_TEXTURE_2D, texture);
        //excuse me the docs say that I have to use "GL_TEXTURE_2D" instead of "texture"
        glGetTextureSubImage(texture, 0, xOffset, yOffset, 0, writeTo.width, writeTo.height, 1, writeTo.type.glFormat, writeTo.type.glType, writeTo.buffer);
    }

    public void downloadFullData(int format, int type, ByteBuffer buffer) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glGetTexImage(GL_TEXTURE_2D, 0, format, type, buffer);
    }
}
