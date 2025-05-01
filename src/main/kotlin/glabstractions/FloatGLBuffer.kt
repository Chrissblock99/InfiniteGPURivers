package me.chriss99.glabstractions

import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL15.glBufferData

class FloatGLBuffer(target: Int) : GLBuffer(target, GL_FLOAT) {
    fun updateData(data: FloatArray, usage: Int): FloatGLBuffer {
        bind()
        glBufferData(target, data, usage)
        return this
    }
}