package me.chriss99

import me.chriss99.glabstractions.GLObject
import org.joml.Vector2i
import org.lwjgl.opengl.GL45.*
import java.nio.ByteBuffer

class Texture2D(private val internalFormat: Int, size: Vector2i) : GLObject {
    private val texture: Int

    private val size: Vector2i

    init {
        this.size = Vector2i(size)

        texture = glGenTextures()
        bind()
        glTexStorage2D(GL_TEXTURE_2D, 1, internalFormat, size.x, size.y)
    }

    fun bindUniformImage(program: Int, bindingUnit: Int, name: String, access: Int) {
        glUseProgram(program)

        glBindImageTexture(bindingUnit, texture, 0, false, 0, access, internalFormat)
        val location: Int = glGetUniformLocation(program, name)
        glUniform1i(location, bindingUnit)
    }

    fun uploadData(offset: Vector2i, data: Array2DBufferWrapper) {
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

    fun downloadData(offset: Vector2i, writeTo: Array2DBufferWrapper) {
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

    fun downloadFullData(format: Int, type: Int, buffer: ByteBuffer) {
        bind()
        glGetTexImage(GL_TEXTURE_2D, 0, format, type, buffer)
    }

    fun getSize(): Vector2i {
        return Vector2i(size)
    }

    override fun bind() {
        glBindTexture(GL_TEXTURE_2D, texture)
    }

    override fun delete() {
        glDeleteTextures(texture)
    }
}