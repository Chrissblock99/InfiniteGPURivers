package me.chriss99.worldmanagement

import glm_.vec2.Vec2i

abstract class AbstractRegionFileManager<T>(worldName: String, fileExtension: String) : RegionFileManager<T> {
    private val fileManager: FileLoadStoreManager<Region<T>> = FileLoadStoreManager(
        "worlds/$worldName", fileExtension,
        ::regionFromBytes, ::regionToBytes
    )

    override fun hasFile(key: Vec2i) = true
    override fun loadFile(chunkCoord: Vec2i) = fileManager.loadFile(chunkCoord)
    override fun saveFile(pos: Vec2i, region: Region<T>) = fileManager.saveFile(region, region.coord)

    protected abstract fun regionFromBytes(bytes: ByteArray, pos: Vec2i): Region<T>
    protected abstract fun regionToBytes(region: Region<T>): ByteArray
}