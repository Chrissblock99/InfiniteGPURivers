package me.chriss99.render

import me.chriss99.util.Util
import glm_.vec2.Vec2i
import me.chriss99.Float2DBufferWrapper

object TerrainVAOGenerator {
    fun heightMapToSimpleVertexes(terrain: Float2DBufferWrapper, water: Float2DBufferWrapper): FloatArray {
        val vertecies = FloatArray(terrain.size.x * terrain.size.y * 2)
        var vertexShift = 0

        for (z in 0..<terrain.size.y) for (x in 0..<terrain.size.x) {
            val terrainHeight: Float = terrain.getFloat(x, z)
            vertecies[vertexShift] = terrainHeight

            val waterHeight: Float = water.getFloat(x, z) - .03f
            vertecies[vertexShift + 1] = terrainHeight + waterHeight - (if (waterHeight <= 0) .1f else 0f)

            vertexShift += 2
        }

        return vertecies
    }

    fun heightMapToSimpleIndex(width: Int, height: Int): IntArray {
        val index = IntArray((width - 1) * (height - 1) * 6)
        var indexShift = 0

        for (z in 0..<height) for (x in 0..<width) {
            if (z == height - 1 || x == width - 1) continue

            index[indexShift + 0] = Util.indexOfXZFlattenedArray(x, z, width)
            index[indexShift + 1] = Util.indexOfXZFlattenedArray(x + 1, z + 1, width)
            index[indexShift + 2] = Util.indexOfXZFlattenedArray(x, z + 1, width)
            index[indexShift + 3] = Util.indexOfXZFlattenedArray(x, z, width)
            index[indexShift + 4] = Util.indexOfXZFlattenedArray(x + 1, z, width)
            index[indexShift + 5] = Util.indexOfXZFlattenedArray(x + 1, z + 1, width)
            indexShift += 6
        }

        return index
    }

    fun heightMapToSimpleVAO(
        terrain: Float2DBufferWrapper,
        water: Float2DBufferWrapper,
        srcPos: Vec2i,
        scale: Int
    ): TerrainVAO {
        val vertices = heightMapToSimpleVertexes(terrain, water)
        val index = heightMapToSimpleIndex(terrain.size.x, terrain.size.y)

        return TerrainVAO(vertices, index, srcPos, terrain.size.x, scale)
    }
}