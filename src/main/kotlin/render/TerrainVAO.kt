package me.chriss99.render

import me.chriss99.glabstractions.VAOImpl
import glm_.vec2.Vec2i

class TerrainVAO(
    triangle: FloatArray, index: IntArray?,
     override val srcPos: Vec2i,
     override val width: Int,
     val scale: Int
) :
    ChunkVAO {
    private val vao: VAOImpl = VAOImpl(index, 2, triangle)

    fun updatePositions(positions: FloatArray) = vao.updateVertices(0, positions)

    override val indexLength: Int
        get() = vao.indexLength

    override fun bind() = vao.bind()
    override fun delete() = vao.delete()
}