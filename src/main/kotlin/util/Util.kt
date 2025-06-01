package me.chriss99.util

import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.math.floor

object Util {
    fun storeArrayInBuffer(array: DoubleArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(array.size * 8)

        for (i in array) buffer.putDouble(i)

        buffer.position(0)
        return buffer
    }

    fun storeArrayInBuffer(array: FloatArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(array.size * 4)

        for (i in array) buffer.putFloat(i)

        buffer.position(0)
        return buffer
    }

    fun storeArrayInBuffer(array: IntArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(array.size * 4)

        for (i in array) buffer.putInt(i)

        buffer.position(0)
        return buffer
    }

    fun indexOfXZFlattenedArray(x: Int, z: Int, xSize: Int): Int {
        return x + z * xSize
    }

    fun floorDiv(a: Int, b: Int): Int {
        return Math.floorDiv(a, b);
    }

    fun floorDiv(v: Vec2i, b: Int): Vec2i {
        return Vec2i(floorDiv(v.x, b), floorDiv(v.y, b))
    }

    fun gridSrcOf(pos: Vec2i, scale: Int): Vec2i {
        return floorDiv(pos, scale).times(scale)
    }
}