package me.chriss99.worldmanagement

import org.joml.Vector2i
import java.util.*
import java.util.function.Function

class Region<T>(coord: Vector2i) {
    val coord: Vector2i = coord
    private val tiles: LinkedHashMap<Vector2i, T> = LinkedHashMap<Vector2i, T>()

    fun addChunk(coord: Vector2i, chunk: T) {
        val oldTile = tiles.put(coord, chunk)
        if (oldTile != null) IllegalStateException("Tile " + coord.x + ", " + coord.y + " was overwritten!").printStackTrace()
    }

    fun getTile(coord: Vector2i, tileGenerator: Function<Vector2i, T>?): T {
        return tiles.computeIfAbsent(coord, tileGenerator!!)
    }

    val allTiles: Set<Map.Entry<Vector2i, T>>
        get() = tiles.entries

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