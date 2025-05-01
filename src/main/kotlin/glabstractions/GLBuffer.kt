package me.chriss99.glabstractions

import org.lwjgl.opengl.GL15.*

open class GLBuffer(target: Int, type: Int) : GLObject {
    private val buffer: Int
    val target: Int
    val type: Int

    init {
        buffer = glGenBuffers()
        this.target = target
        this.type = type
    }

    override fun bind() {
        glBindBuffer(target, buffer)
    }

    override fun delete() {
        glDeleteBuffers(buffer)
    }
}