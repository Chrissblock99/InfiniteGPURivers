package me.chriss99

import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class Byte2DBufferWrapper : Array2DBufferWrapper {
    constructor(buffer: ByteBuffer, size: Vec2i) : super(buffer, Type.BYTE, size)

    constructor(data: Array<ByteArray>) : this(Vec2i(data.size, data[0].size)) {
        for (i in 0..<size.x) for (j in 0..<size.y) putByte(i, j, data[i][j])
    }

    constructor(size: Vec2i) : super(Type.BYTE, size)

    constructor(size: Vec2i, fill: Byte) : this(size) {
        for (i in 0..<size.x) for (j in 0..<size.y) buffer.put(fill)
        buffer.rewind()
    }

    override fun mipMap(): Byte2DBufferWrapper {
        val buffer = BufferUtils.createByteBuffer((size.x / 2) * (size.y / 2) * type.elementSize)

        for (y in 0..size.y step 2)
            for (x in 0..size.x step 2) {
                var avg = 0
                avg += getByte(x, y)
                avg += getByte(x + 1, y)
                avg += getByte(x, y + 1)
                avg += getByte(x + 1, y + 1)
                avg /= 4

                buffer.put(avg.toByte())
            }

        return Byte2DBufferWrapper(buffer, size / 2)
    }

    fun getByte(x: Int, z: Int): Byte {
        return buffer.get((z * size.x + x) * 4)
    }

    fun putByte(x: Int, z: Int, b: Byte) {
        buffer.put((z * size.x + x) * 4, b)
    }
}