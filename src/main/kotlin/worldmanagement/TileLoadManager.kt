package me.chriss99.worldmanagement

import org.joml.Vector2i

interface TileLoadManager<T> {
    fun loadPolicy(tilePos: Vector2i, tile: T): Boolean
    fun loadCommander(): Collection<Vector2i>
}