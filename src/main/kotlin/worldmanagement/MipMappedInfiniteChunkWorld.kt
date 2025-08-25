package me.chriss99.worldmanagement

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import glm_.vec2.Vec2i

class MipMappedInfiniteChunkWorld(
    private val worldName: String,
    private val chunkSize: Int,
    private val regionSize: Int,
    private val chunkGenerator: (pos: Vec2i, chunkSize: Int) -> Chunk,
    private val tileLoadManagerSupplier: (mipmap: Int) -> TileLoadManager<Region<Chunk>>
) {
    private val mipmaps = LinkedHashMap<Int, InfiniteChunkWorld>()

    fun getMipMapLevel(i: Int): InfiniteChunkWorld {
        return mipmaps.computeIfAbsent(i) {
            InfiniteChunkWorld(
                "$worldName/mm$i",
                Array2DBufferWrapper.Type.FLOAT,
                chunkSize,
                regionSize,
                { srcPos: Vec2i, chunkSize: Int -> mipMapChunk(i, srcPos, chunkSize) },
                tileLoadManagerSupplier(i)
            )
        }
    }

    private fun mipMapChunk(mipMapLevel: Int, srcPos: Vec2i, chunkSize: Int): Chunk {
        if (mipMapLevel == 0)
            return chunkGenerator(srcPos, chunkSize)

        val mipMapFrom = getMipMapLevel(mipMapLevel - 1).readArea(Area(srcPos, chunkSize) * 2)
        return Chunk(mipMapFrom.mipMap())
    }

    fun manageLoad() = mipmaps.forEach { it.value.manageLoad() }

    fun unloadAll() = mipmaps.values.forEach { it.unloadAllRegions() }
}