package me.chriss99.worldmanagement

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import org.joml.Vector2i
import java.util.function.BiFunction
import java.util.function.Function

class MipMappedInfiniteChunkWorld(
    private val worldName: String,
    private val chunkSize: Int,
    private val regionSize: Int,
    private val chunkGenerator: BiFunction<Vector2i, Int, Chunk>,
    private val tileLoadManagerSupplier: Function<Int, TileLoadManager<Region<Chunk>>>
) {
    private val mipmaps = LinkedHashMap<Int, InfiniteChunkWorld>()

    fun getMipMapLevel(i: Int): InfiniteChunkWorld {
        return mipmaps.computeIfAbsent(i) { ignored: Int ->
            InfiniteChunkWorld(
                "$worldName/mm$i",
                Array2DBufferWrapper.Type.FLOAT,
                chunkSize,
                regionSize,
                { srcPos: Vector2i, chunkSize: Int -> mipMapChunk(i, srcPos, chunkSize) },
                tileLoadManagerSupplier.apply(i)
            )
        }
    }

    private fun mipMapChunk(mipMapLevel: Int, srcPos: Vector2i, chunkSize: Int): Chunk {
        if (mipMapLevel == 0) return chunkGenerator.apply(srcPos, chunkSize)

        val mipMapFrom = getMipMapLevel(mipMapLevel - 1).readArea(Area(srcPos, chunkSize).mul(2))
        return Chunk(mipMapFrom.mipMap()!!)
    }

    fun unloadAll() {
        for (infiniteWorld in mipmaps.values) infiniteWorld.unloadAllRegions()
    }
}