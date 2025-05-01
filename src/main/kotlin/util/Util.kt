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

    fun properIntDivide(a: Int, b: Int): Int {
        //yes this is horrible, but I was too lazy to do it better
        return floor((a.toDouble()) / (b.toDouble())).toInt()
    }

    fun properIntDivide(v: Vec2i, b: Int): Vec2i {
        return Vec2i(properIntDivide(v.x, b), properIntDivide(v.y, b))
    }

    fun gridSrcOf(pos: Vec2i, scale: Int): Vec2i {
        return properIntDivide(pos, scale).times(scale)
    }
}