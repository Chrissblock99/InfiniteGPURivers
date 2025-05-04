package me.chriss99

import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class CutOutRectangleTLM<T>(renderDistance: Int, initPos: Vec2, var skipArea: Area) : SquareTLM<T>(renderDistance, initPos) {
    override fun loadPolicy(tilePos: Vec2i, tile: T) = super.loadPolicy(tilePos, tile) && tilePos !in skipArea
    override fun loadCommander() = super.loadCommander().filter { it !in skipArea }
}