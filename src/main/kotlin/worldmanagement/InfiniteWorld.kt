package me.chriss99.worldmanagement

import me.chriss99.util.Util
import glm_.vec2.Vec2i

open class InfiniteWorld<T>(
    val chunkSize: Int,
    val regionSize: Int,
    private val generateChunk: (pos: Vec2i, size: Int) -> T,
    regionFileManager: RegionFileManager<T>,
    tileLoadManager: TileLoadManager<Region<T>>
) {
    private val storage: FileBackedTileMap2D<Region<T>> =
        FileBackedTileMap2D(regionFileManager::loadFile, regionFileManager, tileLoadManager)

    operator fun get(pos: Vec2i): T {
        return storage[Util.floorDiv(pos, regionSize)][pos, { generateChunk(it, chunkSize) }]
    }

    fun unloadAllRegions() = storage.unloadAll()
}