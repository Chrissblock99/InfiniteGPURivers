package me.chriss99

import me.chriss99.glabstractions.VAO
import org.joml.Vector2i

interface ChunkVAO : VAO {
    val srcPos: Vector2i
    val width: Int
}