package me.chriss99.util

class FloatArrayList(initialSize: Int = 16) {
    private var array = FloatArray(initialSize)
    private var size = 0

    fun add(i: Int) = add(i.toFloat())

    fun add(f: Float) {
        array[size] = f
        size++
        resizeIfNecessary()
    }

    private fun resizeIfNecessary() {
        if (size < array.size)
            return

        array = array.copyOf(array.size * 2)
    }

    fun getArray() = array.copyOf()
}