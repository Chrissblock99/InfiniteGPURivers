package me.chriss99

import org.joml.Vector2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class Float2DBufferWrapper : Array2DBufferWrapper {
    constructor(buffer: ByteBuffer, size: Vector2i) : super(buffer, Type.FLOAT, size)

    constructor(data: Array<FloatArray>) : this(Vector2i(data.size, data[0].size)) {
        for (i in 0..<size.x) for (j in 0..<size.y) putFloat(i, j, data[i][j])
    }

    constructor(size: Vector2i) : super(Type.FLOAT, size)

    constructor(size: Vector2i, fill: Float) : this(size) {
        for (i in 0..<size.x) for (j in 0..<size.y) buffer.putFloat(fill)
        buffer.rewind()
    }

    override fun mipMap(): Float2DBufferWrapper {
        val buffer = BufferUtils.createByteBuffer((size.x / 2) * (size.y / 2) * type.elementSize)

        var y = 0
        while (y < size.y) {
            var x = 0
            while (x < size.x) {
                var avg = 0f
                avg += getFloat(x, y)
                avg += getFloat(x + 1, y)
                avg += getFloat(x, y + 1)
                avg += getFloat(x + 1, y + 1)
                avg /= 4f

                buffer.putFloat(avg)
                x += 2
            }
            y += 2
        }


        return Float2DBufferWrapper(buffer, Vector2i(size).div(2))
    }

    fun getFloat(x: Int, z: Int): Float {
        return buffer.getFloat((z * size.x + x) * 4)
    }

    fun putFloat(x: Int, z: Int, f: Float) {
        buffer.putFloat((z * size.x + x) * 4, f)
    }

    val array: Array<FloatArray>
        get() {
            val data = Array(size.x) { FloatArray(size.y) }
            for (i in 0..<size.x) for (j in 0..<size.y) data[i][j] = getFloat(i, j)

            return data
        }

    val realArray: Array<FloatArray>
        get() {
            val data = Array(size.y) { FloatArray(size.x) }
            for (i in 0..<size.x) for (j in 0..<size.y) data[j][i] = getFloat(i, j)

            return data
        }
}