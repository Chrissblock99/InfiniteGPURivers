package me.chriss99.worldmanagement

import me.chriss99.Array2DBufferWrapper
import org.joml.Vector2i
import java.nio.ByteBuffer

class ChunkRegionFileManager(worldName: String, type: Array2DBufferWrapper.Type, chunkSize: Int) :
    RegionFileManager<Chunk> {
    private val fileManager: FileLoadStoreManager<Region<Chunk>>
    private val chunkByteSize: Int
    private val chunkDataByteSize: Int
    val type: Array2DBufferWrapper.Type
    val chunkSize: Int

    init {
        this.fileManager = FileLoadStoreManager(
            "worlds/$worldName",
            "region",
            { array: ByteArray, regionCoord: Vector2i -> this.regionFromByteArray(array, regionCoord) },
            { region: Region<Chunk> -> this.regionToByteArray(region) })

        chunkDataByteSize = chunkSize * chunkSize * type.elementSize
        chunkByteSize = chunkDataByteSize + 4 * 2
        this.type = type
        this.chunkSize = chunkSize
    }

    override fun hasFile(key: Vector2i): Boolean {
        return true
    }

    override fun loadFile(regionCoord: Vector2i): Region<Chunk> {
        return fileManager.loadFile(regionCoord)
    }

    override fun saveFile(pos: Vector2i, region: Region<Chunk>) {
        fileManager.saveFile(region, region.coord)
    }

    private fun regionFromByteArray(array: ByteArray, regionCoord: Vector2i): Region<Chunk> {
        val chunkNum = array.size / chunkByteSize
        val region: Region<Chunk> = Region(regionCoord)
        val buffer = ByteBuffer.wrap(array)

        for (i in 0..<chunkNum) {
            val chunkCoord: Vector2i = Vector2i(buffer.getInt(), buffer.getInt())
            val byteArray = ByteArray(chunkDataByteSize)
            buffer[byteArray]
            val data = Array2DBufferWrapper.of(ByteBuffer.wrap(byteArray), type, Vector2i(chunkSize))
            region.addChunk(chunkCoord, Chunk(data))
        }

        return region
    }

    private fun regionToByteArray(region: Region<Chunk>): ByteArray {
        val buffer = ByteBuffer.allocate(region.allTiles.size * chunkByteSize)

        for ((key, value) in region.allTiles) {
            buffer.putInt(key.x)
            buffer.putInt(key.y)
            buffer.put(value.data.buffer)
        }

        return buffer.array()
    }
}