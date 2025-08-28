package me.chriss99

import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class Array2DBufferWrapper(val buffer: ByteBuffer, val type: Type, val size: Vec2i) {
    init {
        val correctCapacity = size.x * size.y * type.elementSize
        require(buffer.capacity() == correctCapacity) { "Buffer has to be of size $correctCapacity but is ${buffer.capacity()}!" }
    }

    constructor(type: Type, size: Vec2i)
            : this(BufferUtils.createByteBuffer(size.x * size.y * type.elementSize), type, size)

    abstract fun mipMap(): Array2DBufferWrapper

    fun slice(z: Int) = of(
            buffer.slice(size.x * type.elementSize * z, size.x * type.elementSize).order(ByteOrder.LITTLE_ENDIAN),
            type, Vec2i(size.x, 1)
        )


    enum class Type(val glFormat: Int, val glType: Int) {
        BYTE(GL_RED, GL_BYTE),
        FLOAT(GL_RED, GL_FLOAT),
        VEC4F(GL_RGBA, GL_FLOAT);

        val elementSize: Int = sizeOf(glFormat, glType)

        private fun sizeOf(format: Int, type: Int) = when (type) {
                GL_BYTE -> 1
                GL_FLOAT, GL_INT -> 4
                GL_DOUBLE -> 8
                else -> throw IllegalArgumentException("Array2DBufferWrapper does not support type: $type")
            } * when (format) {
                GL_RED -> 1
                GL_RGBA -> 4
                else -> throw IllegalArgumentException("Array2DBufferWrapper does not support format: $format")
            }
    }

    companion object {
        fun of(buffer: ByteBuffer, type: Type, size: Vec2i) = when (type) {
                Type.BYTE -> Byte2DBufferWrapper(buffer, size)
                Type.FLOAT -> Float2DBufferWrapper(buffer, size)
                Type.VEC4F -> Vec4f2DBufferWrapper(buffer, size)
            }

        fun of(type: Type, size: Vec2i): Array2DBufferWrapper {
            return of(BufferUtils.createByteBuffer(size.x * size.y * type.elementSize), type, size)
        }
    }
}