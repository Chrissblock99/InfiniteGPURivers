package me.chriss99.util

class FloatArrayList {
    private var array = FloatArray(16)
    private var size = 0

    fun add(f: Float) {
        array[size] = f
        size++
        resizeIfNecessary()
    }

    fun add(i: Int) {
        array[size] = i.toFloat()
        size++
        resizeIfNecessary()
    }

    private fun resizeIfNecessary() {
        if (size < array.size) return

        val newArray = FloatArray(array.size * 2)
        System.arraycopy(array, 0, newArray, 0, size)
        array = newArray
    }

    fun getArray(): FloatArray {
        val array = FloatArray(size)
        System.arraycopy(this.array, 0, array, 0, size)
        return array
    }
}