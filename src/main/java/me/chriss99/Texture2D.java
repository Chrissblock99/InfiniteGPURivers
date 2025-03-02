package me.chriss99;

import org.joml.Vector2i;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;

public class Texture2D {
    private final int texture;

    private final int internalFormat;
    private final Vector2i size;

    public Texture2D(int internalFormat, Vector2i size) {
        this.internalFormat = internalFormat;
        this.size = new Vector2i(size);

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexStorage2D(GL_TEXTURE_2D, 1, internalFormat, size.x, size.y);
    }

    public void bindUniformImage(int program, int bindingUnit, String name, int access) {
        glUseProgram(program);

        glBindImageTexture(bindingUnit, texture, 0, false, 0, access, internalFormat);
        int location = glGetUniformLocation(program, name);
        glUniform1i(location, bindingUnit);
    }

    public void uploadData(Vector2i offset, Array2DBufferWrapper data) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexSubImage2D(GL_TEXTURE_2D, 0, offset.x, offset.y, data.getSize().x, data.getSize().y, data.type.glFormat, data.type.glType, data.buffer);
    }

    public void downloadData(Vector2i offset, Array2DBufferWrapper writeTo) {
        glBindTexture(GL_TEXTURE_2D, texture);
        //excuse me the docs say that I have to use "GL_TEXTURE_2D" instead of "texture"
        glGetTextureSubImage(texture, 0, offset.x, offset.y, 0, writeTo.getSize().x, writeTo.getSize().y, 1, writeTo.type.glFormat, writeTo.type.glType, writeTo.buffer);
    }

    public void downloadFullData(int format, int type, ByteBuffer buffer) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glGetTexImage(GL_TEXTURE_2D, 0, format, type, buffer);
    }

    public Vector2i getSize() {
        return new Vector2i(size);
    }
}
