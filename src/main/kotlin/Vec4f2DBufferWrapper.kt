package me.chriss99

import org.joml.Vector2i
import org.joml.Vector4f
import java.nio.ByteBuffer

class Vec4f2DBufferWrapper : Array2DBufferWrapper {
    constructor(buffer: ByteBuffer, size: Vector2i) : super(buffer, Type.VEC4F, size)

    constructor(size: Vector2i) : super(Type.VEC4F, size)

    override fun mipMap(): Vec4f2DBufferWrapper? {
        throw UnsupportedOperationException("Vec4f can't be mipMapped!")
    }

    fun getVec(x: Int, z: Int): Vector4f {
        return Vector4f(
            buffer.getFloat(((z * size.x + x) * 4 + 0) * 4),
            buffer.getFloat(((z * size.x + x) * 4 + 1) * 4),
            buffer.getFloat(((z * size.x + x) * 4 + 2) * 4),
            buffer.getFloat(((z * size.x + x) * 4 + 3) * 4)
        )
    }

    fun putVec(x: Int, z: Int, v: Vector4f) {
        buffer.putFloat(((z * size.x + x) * 4 + 0) * 4, v.x)
        buffer.putFloat(((z * size.x + x) * 4 + 1) * 4, v.y)
        buffer.putFloat(((z * size.x + x) * 4 + 2) * 4, v.z)
        buffer.putFloat(((z * size.x + x) * 4 + 3) * 4, v.w)
    }
}