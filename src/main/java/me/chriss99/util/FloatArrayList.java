package me.chriss99.util;

public class FloatArrayList {
    private float[] array = new float[16];
    private int size = 0;

    public void add(float f) {
        array[size] = f;
        size++;
        resizeIfNecessary();
    }

    public void add(int i) {
        array[size] = i;
        size++;
        resizeIfNecessary();
    }

    private void resizeIfNecessary() {
        if (size < array.length)
            return;

        float[] newArray = new float[array.length*2];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }

    public float[] getArray() {
        float[] array = new float[size];
        System.arraycopy(this.array, 0, array, 0, size);
        return array;
    }
}
