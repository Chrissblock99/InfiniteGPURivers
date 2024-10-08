package me.chriss99;

import java.nio.ByteBuffer;
import java.sql.Array;

import static org.lwjgl.opengl.GL11.*;

public class Float2DBufferWrapper extends Array2DBufferWrapper {
    public Float2DBufferWrapper(ByteBuffer buffer, int width, int height) {
        super(buffer, GL_RED, GL_FLOAT, width, height);
    }

    public Float2DBufferWrapper(float[][] data) {
        this(data.length, data[0].length);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                putFloat(i, j, data[i][j]);
    }

    public Float2DBufferWrapper(int width, int height) {
        super(GL_RED, GL_FLOAT, width, height);
    }

    public Float2DBufferWrapper(int width, int height, float fill) {
        this(width, height);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                buffer.putFloat(fill);
        buffer.rewind();
    }

    public float getFloat(int x, int z) {
        return buffer.getFloat((z*width + x)*4);
    }

    public void putFloat(int x, int z, float f) {
        buffer.putFloat((z*width + x)*4, f);
    }

    public float[][] getArray() {
        float[][] data = new float[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[i][j] = getFloat(i, j);

        return data;
    }

    public float[][] getRealArray() {
        float[][] data = new float[height][width];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[j][i] = getFloat(i, j);

        return data;
    }
}
