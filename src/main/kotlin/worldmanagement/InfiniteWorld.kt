package me.chriss99.worldmanagement

import me.chriss99.util.Util
import glm_.vec2.Vec2i
import java.util.function.BiFunction

open class InfiniteWorld<T>(
    chunkSize: Int,
    regionSize: Int,
    chunkGenerator: BiFunction<Vec2i, Int, T>,
    regionFileManager: RegionFileManager<T>,
    tileLoadManager: TileLoadManager<Region<T>>
) {
    private val storage: FileBackedTileMap2D<Region<T>>
    private val chunkGenerator: BiFunction<Vec2i, Int, T>

    val chunkSize: Int
    val regionSize: Int

    init {
        this.storage = FileBackedTileMap2D(regionFileManager::loadFile, regionFileManager, tileLoadManager)
        this.chunkGenerator = chunkGenerator

        this.chunkSize = chunkSize
        this.regionSize = regionSize
    }

    fun getTile(pos: Vec2i): T {
        return storage.getTile(Util.properIntDivide(pos, regionSize)).getTile(pos) { vec2i ->
            chunkGenerator.apply(
                vec2i,
                chunkSize
            )
        }
    }

    fun unloadAllRegions() {
        storage.unloadAll()
    }
}