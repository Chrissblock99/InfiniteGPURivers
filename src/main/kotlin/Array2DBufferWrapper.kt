package me.chriss99;

import org.joml.Vector2i;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL11.*;

public abstract sealed class Array2DBufferWrapper permits Float2DBufferWrapper, Vec4f2DBufferWrapper {
    public final ByteBuffer buffer;
    public final Type type;

    protected final Vector2i size;

    protected Array2DBufferWrapper(ByteBuffer buffer, Type type, Vector2i size) {
        int correctCapacity = size.x*size.y*type.elementSize;
        if (buffer.capacity() != correctCapacity)
            throw new IllegalArgumentException("Buffer has to be of size " + correctCapacity + " but is " + buffer.capacity());

        this.buffer = buffer;
        this.type = type;

        this.size = new Vector2i(size);
    }

    protected Array2DBufferWrapper(Type type, Vector2i size) {
        this(BufferUtils.createByteBuffer(size.x*size.y*type.elementSize), type, size);
    }

    public static Array2DBufferWrapper of(ByteBuffer buffer, Type type, Vector2i size) {
        if (buffer == null)
            buffer = BufferUtils.createByteBuffer(size.x*size.y*type.elementSize);

        return switch (type) {
            case FLOAT -> new Float2DBufferWrapper(buffer, size);
            case VEC4F -> new Vec4f2DBufferWrapper(buffer, size);
        };
    }

    public static Array2DBufferWrapper of(Type type, Vector2i size) {
        return of(null, type, size);
    }

    public abstract Array2DBufferWrapper mipMap();


    public Array2DBufferWrapper slice(int z) {
        return of(buffer.slice(size.x*type.elementSize*z, size.x*type.elementSize).order(ByteOrder.LITTLE_ENDIAN), type, new Vector2i(size.x, 1));
    }

    public Vector2i getSize() {
        return new Vector2i(size);
    }


    public enum Type {
        FLOAT(GL_RED, GL_FLOAT),
        VEC4F(GL_RGBA, GL_FLOAT);

        public final int glFormat;
        public final int glType;
        public final int elementSize;

        Type(int glFormat, int glType) {
            this.glFormat = glFormat;
            this.glType = glType;
            elementSize = sizeOf(glFormat, glType);
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
}
