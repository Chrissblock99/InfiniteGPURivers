package me.chriss99

import org.joml.Vector2i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class Array2DBufferWrapper protected constructor(buffer: ByteBuffer, type: Type, size: Vector2i) {
    val buffer: ByteBuffer
    val type: Type

    val size: Vector2i get() = Vector2i(field)

    init {
        val correctCapacity: Int = size.x * size.y * type.elementSize
        require(buffer.capacity() == correctCapacity) { "Buffer has to be of size " + correctCapacity + " but is " + buffer.capacity() }

        this.buffer = buffer
        this.type = type

        this.size = Vector2i(size)
    }

    protected constructor(
        type: Type,
        size: Vector2i
    ) : this(BufferUtils.createByteBuffer(size.x * size.y * type.elementSize), type, size)

    abstract fun mipMap(): Array2DBufferWrapper?


    fun slice(z: Int): Array2DBufferWrapper {
        return of(
            buffer.slice(size.x * type.elementSize * z, size.x * type.elementSize).order(ByteOrder.LITTLE_ENDIAN),
            type,
            Vector2i(size.x, 1)
        )
    }


    enum class Type(val glFormat: Int, val glType: Int) {
        FLOAT(GL_RED, GL_FLOAT),
        VEC4F(GL_RGBA, GL_FLOAT);

        val elementSize: Int

        init {
            elementSize = sizeOf(glFormat, glType)
        }

        private fun sizeOf(format: Int, type: Int): Int {
            var length = when (type) {
                GL_FLOAT, GL_INT -> 4
                GL_DOUBLE -> 8
                else -> throw IllegalArgumentException("Array2DBufferWrapper does not support type: $type")
            }

            length *= when (format) {
                GL_RED -> 1
                GL_RGBA -> 4
                else -> throw IllegalArgumentException("Array2DBufferWrapper does not support format: $format")
            }

            return length
        }
    }

    companion object {
        fun of(buffer: ByteBuffer?, type: Type, size: Vector2i): Array2DBufferWrapper {
            var buffer = buffer
            if (buffer == null) buffer = BufferUtils.createByteBuffer(size.x * size.y * type.elementSize)

            return when (type) {
                Type.FLOAT -> Float2DBufferWrapper(buffer, size)
                Type.VEC4F -> Vec4f2DBufferWrapper(buffer, size)
            }
        }

        fun of(type: Type, size: Vector2i): Array2DBufferWrapper {
            return of(null, type, size)
        }
    }
}