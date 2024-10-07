package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL11.*;

public class Array2DBufferWrapper {
    public final ByteBuffer buffer;
    public final int format;
    public final int type;
    public final int elementSize;

    public final int width;
    public final int height;

    public Array2DBufferWrapper(ByteBuffer buffer, int format, int type, int width, int height) {
        elementSize = sizeOf(format, type);
        int correctCapacity = width*height*elementSize;
        if (buffer.capacity() != correctCapacity)
            throw new IllegalArgumentException("Buffer has to be of size " + correctCapacity + " but is " + buffer.capacity());

        this.buffer = buffer;
        this.format = format;
        this.type = type;

        this.width = width;
        this.height = height;
    }

    public Array2DBufferWrapper(int format, int type, int width, int height) {
        elementSize = sizeOf(format, type);
        this.buffer = BufferUtils.createByteBuffer(width*height*elementSize);
        this.format = format;
        this.type = type;

        this.width = width;
        this.height = height;
    }

    public Array2DBufferWrapper slice(int z) {
        return new Array2DBufferWrapper(buffer.slice(width*elementSize*z, width*elementSize).order(ByteOrder.LITTLE_ENDIAN), format, type, width, 1);
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
}
