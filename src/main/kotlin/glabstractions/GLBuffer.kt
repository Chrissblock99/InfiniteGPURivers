package me.chriss99.glabstractions

import org.lwjgl.opengl.GL15.*

open class GLBuffer(val target: Int, val type: Int) : GLObject {
    private val buffer: Int = glGenBuffers()

    override fun bind() = glBindBuffer(target, buffer)
    override fun delete() = glDeleteBuffers(buffer)
}