package me.chriss99.glabstractions

import org.lwjgl.opengl.GL11.GL_DOUBLE
import org.lwjgl.opengl.GL15.glBufferData

class DoubleGLBuffer(target: Int) : GLBuffer(target, GL_DOUBLE) {
    fun updateData(data: DoubleArray, usage: Int): DoubleGLBuffer {
        bind()
        glBufferData(target, data, usage)
        return this
    }
}