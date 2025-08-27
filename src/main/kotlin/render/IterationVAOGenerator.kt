package me.chriss99.render

import me.chriss99.util.FloatArrayList
import me.chriss99.worldmanagement.iteration.IterableWorld
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import me.chriss99.IterationSurfaceType

object IterationVAOGenerator {
    fun heightMapToIterationVAO(
        srcPosInChunks: Vec2i,
        sizeInChunks: Vec2i,
        iterationInfo: IterableWorld
    ): IterationVAO {
        val vertecies: FloatArrayList = FloatArrayList()

        for (z in 0..<sizeInChunks.y) for (x in 0..<sizeInChunks.x) {
            val position: Vec2i = Vec2i(x, z).plus(srcPosInChunks)
            val surfaceType: IterationSurfaceType = iterationInfo.getIterationSurfaceType(position)
            val iteration: Int = iterationInfo[position].iteration / iterationInfo.chunkSize

            val pos: Vec3i = Vec3i(position.x, iteration, position.y).times(iterationInfo.chunkSize)
            addSurface(vertecies, surfaceType, pos, iterationInfo.chunkSize)
        }

        return IterationVAO(vertecies.getArray(), srcPosInChunks, sizeInChunks.x)
    }

    private fun addSurface(vertecies: FloatArrayList, surfaceType: IterationSurfaceType, pos: Vec3i, scale: Int) {
        val bits: Byte = surfaceType.bits
        val type = (bits.toInt() and 12).toByte()
        val dir = (bits.toInt() and 3).toByte().toInt()
        val otherOrdering = (type.toInt() == 8 || type.toInt() == 12) && (dir == 0 || dir == 3)
        val elevation: Array<IntArray> = surfaceType.surface

        vertecies.add(pos.x)
        vertecies.add(pos.y + elevation[0][0] * scale)
        vertecies.add(pos.z)

        vertecies.add(pos.x + scale)
        vertecies.add(pos.y + elevation[0][1] * scale)
        vertecies.add(pos.z)

        if (!otherOrdering) {
            vertecies.add(pos.x)
            vertecies.add(pos.y + elevation[1][0] * scale)
        } else {
            vertecies.add(pos.x + scale)
            vertecies.add(pos.y + elevation[1][1] * scale)
        }
        vertecies.add(pos.z + scale)

        if (!otherOrdering) {
            vertecies.add(pos.x + scale)
            vertecies.add(pos.y + elevation[0][1] * scale)
            vertecies.add(pos.z)
        }

        vertecies.add(pos.x + scale)
        vertecies.add(pos.y + elevation[1][1] * scale)
        vertecies.add(pos.z + scale)

        vertecies.add(pos.x)
        vertecies.add(pos.y + elevation[1][0] * scale)
        vertecies.add(pos.z + scale)

        if (otherOrdering) {
            vertecies.add(pos.x)
            vertecies.add(pos.y + elevation[0][0] * scale)
            vertecies.add(pos.z)
        }
    }
}