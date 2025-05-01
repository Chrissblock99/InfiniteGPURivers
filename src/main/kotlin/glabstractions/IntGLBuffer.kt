package me.chriss99.glabstractions

import org.lwjgl.opengl.GL11.GL_INT
import org.lwjgl.opengl.GL15.glBufferData

class IntGLBuffer(target: Int) : GLBuffer(target, GL_INT) {
    fun updateData(data: IntArray, usage: Int): IntGLBuffer {
        bind()
        glBufferData(target, data, usage)
        return this
    }
}