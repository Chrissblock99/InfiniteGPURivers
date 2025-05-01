package me.chriss99.worldmanagement

import glm_.vec2.Vec2i

interface TileLoadManager<T> {
    fun loadPolicy(tilePos: Vec2i, tile: T): Boolean
    fun loadCommander(): Collection<Vec2i>
}