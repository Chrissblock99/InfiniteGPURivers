package me.chriss99

import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class Float2DBufferWrapper : Array2DBufferWrapper {
    constructor(buffer: ByteBuffer, size: Vec2i) : super(buffer, Type.FLOAT, size)

    constructor(data: Array<FloatArray>) : this(Vec2i(data.size, data[0].size)) {
        for (i in 0..<size.x) for (j in 0..<size.y) putFloat(i, j, data[i][j])
    }

    constructor(size: Vec2i) : super(Type.FLOAT, size)

    constructor(size: Vec2i, fill: Float) : this(size) {
        for (i in 0..<size.x) for (j in 0..<size.y) buffer.putFloat(fill)
        buffer.rewind()
    }

    override fun mipMap(): Float2DBufferWrapper {
        val buffer = BufferUtils.createByteBuffer((size.x / 2) * (size.y / 2) * type.elementSize)

        for (y in 0..size.y step 2)
            for (x in 0..size.x step 2) {
                var avg = 0f
                avg += getFloat(x, y)
                avg += getFloat(x + 1, y)
                avg += getFloat(x, y + 1)
                avg += getFloat(x + 1, y + 1)
                avg /= 4f

                buffer.putFloat(avg)
            }

        return Float2DBufferWrapper(buffer, size / 2)
    }

    fun getFloat(x: Int, z: Int): Float {
        return buffer.getFloat((z * size.x + x) * 4)
    }

    fun putFloat(x: Int, z: Int, f: Float) {
        buffer.putFloat((z * size.x + x) * 4, f)
    }
}