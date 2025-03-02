package me.chriss99;

import org.joml.Vector2i;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public final class Float2DBufferWrapper extends Array2DBufferWrapper {
    public Float2DBufferWrapper(ByteBuffer buffer, Vector2i size) {
        super(buffer, Type.FLOAT, size);
    }

    public Float2DBufferWrapper(float[][] data) {
        this(new Vector2i(data.length, data[0].length));
        for (int i = 0; i < size.x; i++)
            for (int j = 0; j < size.y; j++)
                putFloat(i, j, data[i][j]);
    }

    public Float2DBufferWrapper(Vector2i size) {
        super(Type.FLOAT, size);
    }

    public Float2DBufferWrapper(Vector2i size, float fill) {
        this(size);
        for (int i = 0; i < size.x; i++)
            for (int j = 0; j < size.y; j++)
                buffer.putFloat(fill);
        buffer.rewind();
    }

    public Float2DBufferWrapper mipMap() {
        ByteBuffer buffer = BufferUtils.createByteBuffer((size.x/2)*(size.y/2)* type.elementSize);

        for (int y = 0; y < size.y; y += 2)
            for (int x = 0; x < size.x; x += 2) {
                float avg = 0;
                avg += getFloat(x, y);
                avg += getFloat(x+1, y);
                avg += getFloat(x, y+1);
                avg += getFloat(x+1, y+1);
                avg /= 4;

                buffer.putFloat(avg);
            }


        return new Float2DBufferWrapper(buffer, new Vector2i(size).div(2));
    }

    public float getFloat(int x, int z) {
        return buffer.getFloat((z*size.x + x)*4);
    }

    public void putFloat(int x, int z, float f) {
        buffer.putFloat((z*size.x + x)*4, f);
    }

    public float[][] getArray() {
        float[][] data = new float[size.x][size.y];
        for (int i = 0; i < size.x; i++)
            for (int j = 0; j < size.y; j++)
                data[i][j] = getFloat(i, j);

        return data;
    }

    public float[][] getRealArray() {
        float[][] data = new float[size.y][size.x];
        for (int i = 0; i < size.x; i++)
            for (int j = 0; j < size.y; j++)
                data[j][i] = getFloat(i, j);

        return data;
    }
}
