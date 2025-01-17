package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public final class Float2DBufferWrapper extends Array2DBufferWrapper {
    public Float2DBufferWrapper(ByteBuffer buffer, int width, int height) {
        super(buffer, Type.FLOAT, width, height);
    }

    public Float2DBufferWrapper(float[][] data) {
        this(data.length, data[0].length);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                putFloat(i, j, data[i][j]);
    }

    public Float2DBufferWrapper(int width, int height) {
        super(Type.FLOAT, width, height);
    }

    public Float2DBufferWrapper(int width, int height, float fill) {
        this(width, height);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                buffer.putFloat(fill);
        buffer.rewind();
    }

    public Float2DBufferWrapper mipMap() {
        ByteBuffer buffer = BufferUtils.createByteBuffer((width/2)*(height/2)* type.elementSize);

        for (int y = 0; y < height; y += 2)
            for (int x = 0; x < width; x += 2) {
                float avg = 0;
                avg += getFloat(x, y);
                avg += getFloat(x+1, y);
                avg += getFloat(x, y+1);
                avg += getFloat(x+1, y+1);
                avg /= 4;

                buffer.putFloat(avg);
            }


        return new Float2DBufferWrapper(buffer, width/2, height/2);
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
