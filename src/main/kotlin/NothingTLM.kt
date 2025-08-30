package me.chriss99

import me.chriss99.worldmanagement.TileLoadManager
import glm_.vec2.Vec2i

class NothingTLM<T> : TileLoadManager<T> {
    override fun loadPolicy(tilePos: Vec2i, tile: T) = false
    override fun loadCommander() = listOf<Vec2i>()
}