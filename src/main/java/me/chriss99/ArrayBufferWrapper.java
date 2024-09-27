package me.chriss99;

import java.nio.ByteBuffer;

public class ArrayBufferWrapper {
    public final ByteBuffer buffer;
    public final int format;
    public final int type;

    public final int width;
    public final int height;

    public ArrayBufferWrapper(ByteBuffer buffer, int format, int type, int width, int height) {
        this.buffer = buffer;
        this.format = format;
        this.type = type;

        this.width = width;
        this.height = height;
    }

    public float getFloat(int x, int z) {
        return buffer.getFloat((z*width + x)*4);
    }

    public void putFloat(int x, int z, float f) {
        buffer.putFloat((z*width + x)*4, f);
    }
}
