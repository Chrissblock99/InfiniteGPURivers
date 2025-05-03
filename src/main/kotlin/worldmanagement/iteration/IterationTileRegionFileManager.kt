package me.chriss99.worldmanagement.iteration

import me.chriss99.worldmanagement.AbstractRegionFileManager
import me.chriss99.worldmanagement.Region
import glm_.vec2.Vec2i
import java.nio.ByteBuffer

class IterationTileRegionFileManager(worldName: String) : AbstractRegionFileManager<IterationTile>(worldName) {

    protected override fun regionFromBytes(bytes: ByteArray, pos: Vec2i): Region<IterationTile> {
        val region: Region<IterationTile> = Region(pos)
        val buffer = ByteBuffer.wrap(bytes)

        while (buffer.hasRemaining()) {
            val tilePos: Vec2i = Vec2i(buffer.getInt(), buffer.getInt())
            val iteration = buffer.getInt()
            val bits = buffer.get()

            val horizontal = when (bits.toInt() and 0b11) {
                0b00 -> 0
                0b01 -> 1
                0b11 -> -1
                else -> throw IllegalStateException("Unexpected bit pattern: $bits")
            }
            val vertical = when ((bits.toInt() ushr 2) and 0b11) {
                0b00 -> 0
                0b01 -> 1
                0b11 -> -1
                else -> throw IllegalStateException("Unexpected bit pattern: $bits")
            }

            region.addChunk(tilePos, IterationTile(horizontal, vertical, iteration))
        }

        return region
    }

    protected override fun regionToBytes(region: Region<IterationTile>): ByteArray {
        val tileEntrySet = region.allTiles
        val array = ByteArray(tileEntrySet.size * (4 + 4 + 4 + 1))
        val buffer = ByteBuffer.wrap(array)

        for ((key, tile) in tileEntrySet) {
            buffer.putInt(key.x).putInt(key.y)
            buffer.putInt(tile.iteration)
            buffer.put(((tile.horizontal shl 2) or (tile.vertical and 3)).toByte())
        }

        return array
    }
}