package me.chriss99.worldmanagement

import glm_.vec2.Vec2i
import java.util.*

class Region<T>(val coord: Vec2i) {
    private val tiles: LinkedHashMap<Vec2i, T> = LinkedHashMap<Vec2i, T>()
    val allTiles get() = tiles.entries.toList()

    fun addChunk(coord: Vec2i, chunk: T) {
        val oldTile = tiles.put(coord, chunk)
        if (oldTile != null)
            IllegalStateException("Tile " + coord.x + ", " + coord.y + " was overwritten!").printStackTrace()
    }

    operator fun get(coord: Vec2i, tileGenerator: (pos: Vec2i) -> T): T = tiles.computeIfAbsent(coord, tileGenerator)

    override fun toString() = "Region(coord=$coord, tiles=$tiles)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Region<*>) return false

        if (coord != other.coord) return false
        if (tiles != other.tiles) return false

        return true
    }

    override fun hashCode() = Objects.hash(coord, tiles)
}