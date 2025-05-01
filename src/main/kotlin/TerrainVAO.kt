package me.chriss99

import me.chriss99.glabstractions.VAOImpl
import org.joml.Vector2i

class TerrainVAO(triangle: FloatArray, index: IntArray?, srcPos: Vector2i, width: Int, scale: Int) :
    ChunkVAO {
    private val vao: VAOImpl
    override val srcPos: Vector2i get() = Vector2i(field)
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