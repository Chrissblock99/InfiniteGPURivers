package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class ArrayBufferWrapper {
    public final ByteBuffer buffer;
    public final int format;
    public final int type;

    public final int width;
    public final int height;

    public ArrayBufferWrapper(ByteBuffer buffer, int format, int type, int width, int height) {
        int correctCapacity = width*height*sizeOf(format, type);
        if (buffer.capacity() != correctCapacity)
            throw new IllegalArgumentException("Buffer has to be of size " + correctCapacity + " but is " + buffer.capacity());

        this.buffer = buffer;
        this.format = format;
        this.type = type;

        this.width = width;
        this.height = height;
    }

    public ArrayBufferWrapper(int format, int type, int width, int height) {
        this.buffer = BufferUtils.createByteBuffer(width*height*sizeOf(format, type));
        this.format = format;
        this.type = type;

        this.width = width;
        this.height = height;
    }

    private static int sizeOf(int format, int type) {
        int length = switch (type) {
            case GL_FLOAT, GL_INT -> 4;
            case GL_DOUBLE -> 8;
            default -> throw new IllegalArgumentException("Array2DBufferWrapper does not support type: " + type);
        };

        length *= switch (format) {
            case GL_RED -> 1;
            case GL_RGBA -> 4;
            default -> throw new IllegalArgumentException("Array2DBufferWrapper does not support format: " + format);
        };

        return length;
    }

    public float getFloat(int x, int z) {
        return buffer.getFloat((z*width + x)*4);
    }

    public void putFloat(int x, int z, float f) {
        buffer.putFloat((z*width + x)*4, f);
    }
}
