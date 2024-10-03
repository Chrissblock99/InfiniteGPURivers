package me.chriss99;

import java.nio.ByteBuffer;
import java.sql.Array;

import static org.lwjgl.opengl.GL11.*;

public class Float2DBufferWrapper extends Array2DBufferWrapper {
    public Float2DBufferWrapper(ByteBuffer buffer, int width, int height) {
        super(buffer, GL_RED, GL_FLOAT, width, height);
    }

    public Float2DBufferWrapper(int width, int height) {
        super(GL_RED, GL_FLOAT, width, height);
    }

    public float getFloat(int x, int z) {
        return buffer.getFloat((z*width + x)*4);
    }

    public void putFloat(int x, int z, float f) {
        buffer.putFloat((z*width + x)*4, f);
    }
}
