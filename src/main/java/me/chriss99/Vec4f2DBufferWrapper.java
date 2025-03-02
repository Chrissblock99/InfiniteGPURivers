package me.chriss99;

import org.joml.Vector2i;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public final class Vec4f2DBufferWrapper extends Array2DBufferWrapper {
    public Vec4f2DBufferWrapper(ByteBuffer buffer, Vector2i size) {
        super(buffer, Type.VEC4F, size);
    }

    public Vec4f2DBufferWrapper(Vector2i size) {
        super(Type.VEC4F, size);
    }

    public Vec4f2DBufferWrapper mipMap() {
        throw new UnsupportedOperationException("Vec4f can't be mipMapped!");
    }

    public Vector4f getVec(int x, int z) {
        return new Vector4f(
                buffer.getFloat(((z*size.x + x)*4 + 0)*4),
                buffer.getFloat(((z*size.x + x)*4 + 1)*4),
                buffer.getFloat(((z*size.x + x)*4 + 2)*4),
                buffer.getFloat(((z*size.x + x)*4 + 3)*4));
    }

    public void putVec(int x, int z, Vector4f v) {
        buffer.putFloat(((z*size.x + x)*4 + 0)*4, v.x);
        buffer.putFloat(((z*size.x + x)*4 + 1)*4, v.y);
        buffer.putFloat(((z*size.x + x)*4 + 2)*4, v.z);
        buffer.putFloat(((z*size.x + x)*4 + 3)*4, v.w);
    }
}
