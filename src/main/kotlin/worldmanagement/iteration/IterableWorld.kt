package me.chriss99.worldmanagement.iteration

import me.chriss99.IterationSurfaceType
import me.chriss99.worldmanagement.InfiniteWorld
import me.chriss99.worldmanagement.Region
import me.chriss99.worldmanagement.TileLoadManager
import glm_.vec2.Vec2i

class IterableWorld(
    worldName: String,
    chunkSize: Int,
    regionSize: Int,
    tileLoadManager: TileLoadManager<Region<IterationTile>>
) :
    InfiniteWorld<IterationTile>(
        chunkSize,
        regionSize,
        { _, _ -> IterationTile(0, 0, 0) },
        IterationTileRegionFileManager(worldName),
        tileLoadManager
    ) {
    fun getIterationSurfaceType(pos: Vec2i): IterationSurfaceType {
        val v0: Int = get(pos).vertical and 0b11
        val v1: Int = get(pos.plus(1, 0)).vertical and 0b11
        val h0: Int = get(pos).horizontal and 0b11
        val h1: Int = get(pos.plus(0, 1)).horizontal and 0b11

        val combined = h1 or (v0 shl 2) or (v1 shl 4) or (h0 shl 6)
        val bits = when (combined) {
            0b00000000 -> 0b0000

            0b00111100 -> 0b0100
            0b01000001 -> 0b0101
            0b11000011 -> 0b0110
            0b00010100 -> 0b0111

            0b00110011 -> 0b1000
            0b00001101 -> 0b1001
            0b11010000 -> 0b1010
            0b01000100 -> 0b1011

            0b11001100 -> 0b1100
            0b01110000 -> 0b1101
            0b00000111 -> 0b1110
            0b00010001 -> 0b1111
            else -> throw IllegalStateException("Illegal combination: $combined")
        }.toByte()

        return IterationSurfaceType(bits)
    }
}