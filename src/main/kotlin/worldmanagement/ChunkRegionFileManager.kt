package me.chriss99.worldmanagement

import me.chriss99.Array2DBufferWrapper
import glm_.vec2.Vec2i
import java.nio.ByteBuffer

class ChunkRegionFileManager(worldName: String, val type: Array2DBufferWrapper.Type, val chunkSize: Int) :
    AbstractRegionFileManager<Chunk>(worldName, "region") {
    private val chunkDataByteSize: Int = chunkSize * chunkSize * type.elementSize
    private val chunkByteSize: Int = chunkDataByteSize + 4 * 2

    override fun regionFromBytes(array: ByteArray, regionCoord: Vec2i): Region<Chunk> {
        val chunkNum = array.size / chunkByteSize
        val region: Region<Chunk> = Region(regionCoord)
        val buffer = ByteBuffer.wrap(array)

        for (i in 0..<chunkNum) {
            val chunkCoord: Vec2i = Vec2i(buffer.getInt(), buffer.getInt())
            val byteArray = ByteArray(chunkDataByteSize)
            buffer.get(byteArray)
            val data = Array2DBufferWrapper.of(ByteBuffer.wrap(byteArray), type, Vec2i(chunkSize))
            region.addChunk(chunkCoord, Chunk(data))
        }

        return region
    }

    override fun regionToBytes(region: Region<Chunk>): ByteArray {
        val buffer = ByteBuffer.allocate(region.allTiles.size * chunkByteSize)

        for ((key, value) in region.allTiles) {
            buffer.putInt(key.x)
            buffer.putInt(key.y)
            buffer.put(value.data.buffer)
        }

        return buffer.array()
    }
}