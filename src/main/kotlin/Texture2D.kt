package me.chriss99

import me.chriss99.glabstractions.GLObject
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL45.*

class Texture2D(val type: Array2DBufferWrapper.Type, val size: Vec2i) : GLObject {
    init {
        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        if (size.anyGreaterThan(maxSize))
            throw RuntimeException("Max texture size is $maxSize, but a texture of size $size was requested!")
    }

    private val texture: Int = glGenTextures()

    init {
        bind()
        glTexStorage2D(GL_TEXTURE_2D, 1, type.glInternalFormat, size.x, size.y)
    }

    fun bindUniformImage(program: Int, bindingUnit: Int, name: String, access: Int) {
        glUseProgram(program)

        glBindImageTexture(bindingUnit, texture, 0, false, 0, access, type.glInternalFormat)
        val location: Int = glGetUniformLocation(program, name)
        glUniform1i(location, bindingUnit)
    }

    fun uploadData(offset: Vec2i, data: Array2DBufferWrapper) {
        if (data.type != type)
            throw IllegalArgumentException("Tried to upload data with incorrect type! Should be $type but is ${data.type}.")

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

    fun downloadData(area: Area): Array2DBufferWrapper {
        val bufferWrapper = Array2DBufferWrapper.of(type, area.size)
        bind()
        //excuse me the docs say that I have to use "GL_TEXTURE_2D" instead of "texture"
        glGetTextureSubImage(
            texture,
            0,
            area.srcPos.x,
            area.srcPos.y,
            0,
            area.size.x,
            area.size.y,
            1,
            type.glFormat,
            type.glType,
            bufferWrapper.buffer
        )
        return bufferWrapper
    }

    fun downloadFullData(buffer: Array2DBufferWrapper) {
        bind()
        glGetTexImage(GL_TEXTURE_2D, 0, buffer.type.glFormat, buffer.type.glType, buffer.buffer)
    }

    override fun bind() = glBindTexture(GL_TEXTURE_2D, texture)
    override fun delete() = glDeleteTextures(texture)
}