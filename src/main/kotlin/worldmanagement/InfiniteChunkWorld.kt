package me.chriss99.worldmanagement

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import me.chriss99.util.Util
import glm_.vec2.Vec2i
import kotlin.math.max
import kotlin.math.min

class InfiniteChunkWorld(
    worldName: String,
    val type: Array2DBufferWrapper.Type,
    chunkSize: Int,
    regionSize: Int,
    chunkGenerator: (pos: Vec2i, size: Int) -> Chunk,
    tileLoadManager: TileLoadManager<Region<Chunk>>
) :
    InfiniteWorld<Chunk>(
        chunkSize,
        regionSize,
        chunkGenerator,
        ChunkRegionFileManager(worldName, type, chunkSize),
        tileLoadManager
    ) {
    fun readArea(area: Area): Array2DBufferWrapper {
        return readWriteArea(area.srcPos, Array2DBufferWrapper.of(type, area.size), true)
    }

    fun writeArea(pos: Vec2i, data: Array2DBufferWrapper) {
        readWriteArea(pos, data, false)
    }

    private fun readWriteArea(pos: Vec2i, data: Array2DBufferWrapper, read: Boolean): Array2DBufferWrapper {
        val x: Int = pos.x
        val y: Int = pos.y
        val width: Int = data.size.x
        val height: Int = data.size.y


        val chunkX: Int = Util.properIntDivide(x, chunkSize)
        val chunkY: Int = Util.properIntDivide(y, chunkSize)
        val chunksX: Int = Util.properIntDivide(x + width - 1, chunkSize) - chunkX + 1
        val chunksY: Int = Util.properIntDivide(y + height - 1, chunkSize) - chunkY + 1

        for (currentChunkX in chunkX..<chunkX + chunksX) for (currentChunkY in chunkY..<chunkY + chunksY) {
            val currentChunk: Chunk = get(Vec2i(currentChunkX, currentChunkY))

            val currentChunkMinX = ((max(
                (currentChunkX * chunkSize).toDouble(),
                x.toDouble()
            ) % chunkSize + chunkSize) % chunkSize).toInt()
            val currentChunkMaxX =
                ((min(
                    (currentChunkX * chunkSize + chunkSize - 1).toDouble(),
                    (x + width - 1).toDouble()
                ) % chunkSize + chunkSize) % chunkSize).toInt()
            val currentChunkMinY = ((max(
                (currentChunkY * chunkSize).toDouble(),
                y.toDouble()
            ) % chunkSize + chunkSize) % chunkSize).toInt()
            val currentChunkMaxY =
                ((min(
                    (currentChunkY * chunkSize + chunkSize - 1).toDouble(),
                    (y + height - 1).toDouble()
                ) % chunkSize + chunkSize) % chunkSize).toInt()

            for (i in currentChunkMinY..currentChunkMaxY) {
                var src = data.slice(currentChunkY * chunkSize + i - y)
                var srcPos = currentChunkX * chunkSize + currentChunkMinX - x
                var dest: Array2DBufferWrapper = currentChunk.data.slice(i)
                var destPos = currentChunkMinX

                if (read) {
                    val temp = dest
                    dest = src
                    src = temp

                    val tempPos = destPos
                    destPos = srcPos
                    srcPos = tempPos
                }

                dest.buffer.put(
                    destPos * type.elementSize,
                    src.buffer,
                    srcPos * type.elementSize,
                    (currentChunkMaxX - currentChunkMinX + 1) * type.elementSize
                )
            }
        }

        return data
    }
}