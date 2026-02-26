package me.chriss99

import me.chriss99.glabstractions.GLObject
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL45.*

class Texture2D(private val internalFormat: Int, val size: Vec2i) : GLObject {
    init {
        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        if (size.anyGreaterThan(maxSize))
            throw RuntimeException("Max texture size is $maxSize, but a texture of size $size was requested!")
    }

    private val texture: Int = glGenTextures()

    init {
        bind()
        glTexStorage2D(GL_TEXTURE_2D, 1, internalFormat, size.x, size.y)
    }

    fun bindUniformImage(program: Int, bindingUnit: Int, name: String, access: Int) {
        glUseProgram(program)

        glBindImageTexture(bindingUnit, texture, 0, false, 0, access, internalFormat)
        val location: Int = glGetUniformLocation(program, name)
        glUniform1i(location, bindingUnit)
    }

    fun uploadData(offset: Vec2i, data: Array2DBufferWrapper) {
        bind()
        glTexSubImage2D(
            GL_TEXTURE_2D,
            0,
            offset.x,
            offset.y,
            data.size.x,
            data.size.y,
            data.type.glFormat,
            data.type.glType,
            data.buffer
        )
    }

    fun downloadData(offset: Vec2i, writeTo: Array2DBufferWrapper) {
        bind()
        //excuse me the docs say that I have to use "GL_TEXTURE_2D" instead of "texture"
        glGetTextureSubImage(
            texture,
            0,
            offset.x,
            offset.y,
            0,
            writeTo.size.x,
            writeTo.size.y,
            1,
            writeTo.type.glFormat,
            writeTo.type.glType,
            writeTo.buffer
        )
    }

    fun downloadFullData(buffer: Array2DBufferWrapper) {
        bind()
        glGetTexImage(GL_TEXTURE_2D, 0, buffer.type.glFormat, buffer.type.glType, buffer.buffer)
    }

    override fun bind() = glBindTexture(GL_TEXTURE_2D, texture)
    override fun delete() = glDeleteTextures(texture)
}