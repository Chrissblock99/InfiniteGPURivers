package me.chriss99

import me.chriss99.glabstractions.VAOImpl
import glm_.vec2.Vec2i

class IterationVAO(triangle: FloatArray, srcPos: Vec2i, width: Int) : ChunkVAO {
    private val vao: VAOImpl
    override val srcPos: Vec2i get() = Vec2i(field)
    override val width: Int

    init {
        vao = VAOImpl(null, 3, triangle)
        this.srcPos = srcPos
        this.width = width
    }

    fun updatePositions(positions: FloatArray) {
        vao.updateVertices(0, positions)
    }

    override val indexLength: Int
        get() = vao.indexLength

    override fun bind() {
        vao.bind()
    }

    override fun delete() {
        vao.delete()
    }
}