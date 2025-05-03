package me.chriss99.worldmanagement

import glm_.vec2.Vec2i
import java.util.*
import java.util.function.Function

class Region<T>(coord: Vec2i) {
    val coord: Vec2i = coord
    private val tiles: LinkedHashMap<Vec2i, T> = LinkedHashMap<Vec2i, T>()
    val allTiles get() = tiles.entries.toList()

    fun addChunk(coord: Vec2i, chunk: T) {
        val oldTile = tiles.put(coord, chunk)
        if (oldTile != null)
            IllegalStateException("Tile " + coord.x + ", " + coord.y + " was overwritten!").printStackTrace()
    }

    operator fun get(coord: Vec2i, tileGenerator: Function<Vec2i, T>): T = tiles.computeIfAbsent(coord, tileGenerator)

    override fun toString(): String {
        return "Region{" +
                "coord=" + coord +
                ", tiles=" + tiles +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (o == null || javaClass != o.javaClass) return false
        val region = o as Region<*>
        return coord == region.coord && tiles == region.tiles
    }

    override fun hashCode(): Int {
        return Objects.hash(coord, tiles)
    }
}