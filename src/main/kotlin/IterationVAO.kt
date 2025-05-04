package me.chriss99

import me.chriss99.glabstractions.VAOImpl
import glm_.vec2.Vec2i

class IterationVAO(triangle: FloatArray, override val srcPos: Vec2i, override val width: Int) : ChunkVAO {
    private val vao: VAOImpl = VAOImpl(null, 3, triangle)

    fun updatePositions(positions: FloatArray) = vao.updateVertices(0, positions)

    override val indexLength: Int
        get() = vao.indexLength

    override fun bind() = vao.bind()
    override fun delete() = vao.delete()
}