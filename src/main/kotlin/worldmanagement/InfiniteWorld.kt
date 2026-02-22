package me.chriss99.worldmanagement

import glm_.vec2.Vec2i
import me.chriss99.util.Util.floorDiv

open class InfiniteWorld<T>(
    val chunkSize: Int,
    val regionSize: Int,
    private val generateChunk: (pos: Vec2i, size: Int) -> T,
    regionFileManager: RegionFileManager<T>,
    tileLoadManager: TileLoadManager<Region<T>>
) {
    private val storage: FileBackedTileMap2D<Region<T>> =
        FileBackedTileMap2D(regionFileManager::loadFile, regionFileManager, tileLoadManager)
    val allTiles get() = storage.allTiles

    operator fun get(pos: Vec2i): T {
        return storage[pos floorDiv regionSize][pos, { generateChunk(it, chunkSize) }]
    }

    fun manageLoad() = storage.manageLoad()

    fun unloadAllRegions() = storage.unloadAll()
}