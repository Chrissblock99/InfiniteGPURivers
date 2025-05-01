package me.chriss99

import me.chriss99.glabstractions.VAOImpl
import glm_.vec2.Vec2i

class TerrainVAO(triangle: FloatArray, index: IntArray?, srcPos: Vec2i, width: Int, scale: Int) :
    ChunkVAO {
    private val vao: VAOImpl
    override val srcPos: Vec2i get() = Vec2i(field)
    override val width: Int
    val scale: Int

    init {
        vao = VAOImpl(index, 2, triangle)
        this.srcPos = srcPos
        this.width = width
        this.scale = scale
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