package me.chriss99;

import org.joml.Vector4f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public final class Vec4f2DBufferWrapper extends Array2DBufferWrapper {
    public Vec4f2DBufferWrapper(ByteBuffer buffer, int width, int height) {
        super(buffer, Type.VEC4F, width, height);
    }

    public Vec4f2DBufferWrapper(int width, int height) {
        super(Type.VEC4F, width, height);
    }

    public Vec4f2DBufferWrapper mipMap() {
        throw new UnsupportedOperationException("Vec4f can't be mipMapped!");
    }

    public Vector4f getVec(int x, int z) {
        return new Vector4f(
                buffer.getFloat(((z*width + x)*4 + 0)*4),
                buffer.getFloat(((z*width + x)*4 + 1)*4),
                buffer.getFloat(((z*width + x)*4 + 2)*4),
                buffer.getFloat(((z*width + x)*4 + 3)*4));
    }

    public void putVec(int x, int z, Vector4f v) {
        buffer.putFloat(((z*width + x)*4 + 0)*4, v.x);
        buffer.putFloat(((z*width + x)*4 + 1)*4, v.y);
        buffer.putFloat(((z*width + x)*4 + 2)*4, v.z);
        buffer.putFloat(((z*width + x)*4 + 3)*4, v.w);
    }
}
