package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class Util {
    public static ByteBuffer storeArrayInBuffer(double[] array) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(array.length * 8);

        for(double i : array)
            buffer.putDouble(i);

        buffer.position(0);
        return buffer;
    }

    public static ByteBuffer storeArrayInBuffer(float[] array) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(array.length * 4);

        for(float i : array)
            buffer.putFloat(i);

        buffer.position(0);
        return buffer;
    }

    public static ByteBuffer storeArrayInBuffer(int[] array) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(array.length * 4);

        for(int i : array)
            buffer.putInt(i);

        buffer.position(0);
        return buffer;
    }

    static int indexOfXZFlattenedArray(int x, int z, int xSize) {
        return x + z*xSize;
    }

    public static int properIntDivide(int a, int b) {
        //yes this is horrible, but I was too lazy to do it better
        return (int) Math.floor(((double) a)/((double) b));
    }
}
