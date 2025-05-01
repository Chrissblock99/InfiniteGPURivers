package me.chriss99

import org.joml.Vector2i

class ColoredChunkVAO(
    triangle: DoubleArray, color: DoubleArray, index: IntArray, srcPos: Vector2i,
    override val width: Int
) :
    ColoredVAO(triangle, color, index), ChunkVAO {
    override val srcPos: Vector2i = srcPos
}