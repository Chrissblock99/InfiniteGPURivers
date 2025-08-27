package me.chriss99.render

import glm_.vec2.Vec2i

class ColoredChunkVAO(
    triangle: DoubleArray, color: DoubleArray, index: IntArray, override val srcPos: Vec2i, override val width: Int
) : ColoredVAO(triangle, color, index), ChunkVAO