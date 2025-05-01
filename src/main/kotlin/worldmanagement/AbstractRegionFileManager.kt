package me.chriss99.worldmanagement

import glm_.vec2.Vec2i

abstract class AbstractRegionFileManager<T>(worldName: String) : RegionFileManager<T> {
    private val fileManager: FileLoadStoreManager<Region<T>>

    init {
        fileManager = FileLoadStoreManager(
            "worlds/$worldName",
            "quadtree",
            { bytes: ByteArray, pos: Vec2i -> this.regionFromBytes(bytes, pos) },
            { region: Region<T> -> this.regionToBytes(region) })
    }

    override fun hasFile(key: Vec2i): Boolean {
        return true
    }

    override fun loadFile(chunkCoord: Vec2i): Region<T> {
        return fileManager.loadFile(chunkCoord)
    }

    override fun saveFile(pos: Vec2i, region: Region<T>) {
        fileManager.saveFile(region, region.coord)
    }


    protected abstract fun regionFromBytes(bytes: ByteArray, pos: Vec2i): Region<T>

    protected abstract fun regionToBytes(region: Region<T>): ByteArray
}