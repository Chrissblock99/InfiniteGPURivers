package me.chriss99.glabstractions

class VBO<T : GLBuffer>(val buffer: T, val vertexSize: Int) {
    fun delete() {
        buffer.delete()
    }
}