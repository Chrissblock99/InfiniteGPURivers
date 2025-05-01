package me.chriss99

import me.chriss99.glabstractions.VAO
import glm_.vec2.Vec2i

interface ChunkVAO : VAO {
    val srcPos: Vec2i
    val width: Int
}