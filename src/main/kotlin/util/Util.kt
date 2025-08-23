package me.chriss99.util

import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

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

    fun spiral(n: Int, scale: Int, srcPos: Vec2i): Vec2i {
        return spiral(n) * scale + ((srcPos/scale)*scale)
    }

    val corners: Array<Vec2i> = arrayOf(Vec2i(1,-1), Vec2i(1,1), Vec2i(-1,1), Vec2i(-1,-1))
    val direction: Array<Vec2i> = arrayOf(Vec2i(0,1), Vec2i(-1,0), Vec2i(0,-1), Vec2i(1,0))
    fun spiral(n: Int): Vec2i {
        if (n == 0)
            return Vec2i(0)

        val r = ring(n)
        val localN = (n - area(r-1))
        val rSizeDiv4 = (area(r)-area(r-1))/4
        val side = localN/rSizeDiv4
        val sideN = localN-side*rSizeDiv4 + 1

        return corners[side]*r + direction[side]*sideN
    }

    fun area(n: Int) = (2*n+1)*(2*n+1)
    fun ring(n: Int) = ceil((sqrt((n+1).toDouble())-1.0)/2.0).toInt()
}