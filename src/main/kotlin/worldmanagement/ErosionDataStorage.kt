package me.chriss99.worldmanagement

import me.chriss99.*
import me.chriss99.worldmanagement.iteration.IterableWorld
import me.chriss99.worldmanagement.iteration.IterationTile
import org.joml.Vector2i

class ErosionDataStorage(
    worldName: String,
    val chunkSize: Int,
    val regionSize: Int,
    val iterationChunkSize: Int,
    val iterationRegionSize: Int
) {
    private val terrainGenerator = TerrainGenerator(chunkSize)

    val mipMappedTerrain: MipMappedInfiniteChunkWorld
    val mipMappedWater: MipMappedInfiniteChunkWorld

    val terrain: InfiniteChunkWorld
    val water: InfiniteChunkWorld
    val sediment: InfiniteChunkWorld
    val hardness: InfiniteChunkWorld

    val waterOutflow: InfiniteChunkWorld
    val sedimentOutflow: InfiniteChunkWorld

    val thermalOutflow1: InfiniteChunkWorld
    val thermalOutflow2: InfiniteChunkWorld

    val iterationInfo: IterableWorld

    val tileLoadManager: TileLoadManager<Region<Chunk>> = LeakingTLM()
    val tileLoadManager2: TileLoadManager<Region<IterationTile>> = LeakingTLM()

    init {
        mipMappedTerrain = MipMappedInfiniteChunkWorld(
            "$worldName/terrain", chunkSize, regionSize,
            { chunkPos: Vector2i?, chunkSize: Int? ->
                terrainGenerator.generateChunk(
                    chunkPos!!, chunkSize!!
                )
            },
            { i: Int -> tileLoadManager })
        mipMappedWater = MipMappedInfiniteChunkWorld(
            "$worldName/water", chunkSize, regionSize,
            { vector2i: Vector2i?, chunkSize1: Int? ->
                Chunk(
                    Float2DBufferWrapper(
                        Vector2i(
                            chunkSize1!!
                        )
                    )
                )
            },
            { i: Int -> tileLoadManager })

        terrain = mipMappedTerrain.getMipMapLevel(0)
        water = mipMappedWater.getMipMapLevel(0)
        sediment = InfiniteChunkWorld(
            "$worldName/sediment", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { vector2i: Vector2i, chunkSize1: Int ->
                Chunk(
                    Float2DBufferWrapper(
                        Vector2i(
                            chunkSize1
                        )
                    )
                )
            }, tileLoadManager
        )
        hardness = InfiniteChunkWorld(
            "$worldName/hardness", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { vector2i: Vector2i?, chunkSize1: Int? ->
                Chunk(
                    Float2DBufferWrapper(
                        Vector2i(
                            chunkSize1!!
                        ), 1f
                    )
                )
            }, tileLoadManager
        )

        waterOutflow = InfiniteChunkWorld(
            "$worldName/waterOutflow", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { vector2i: Vector2i?, chunkSize1: Int? ->
                Chunk(
                    Vec4f2DBufferWrapper(
                        Vector2i(
                            chunkSize1!!
                        )
                    )
                )
            }, tileLoadManager
        )
        sedimentOutflow = InfiniteChunkWorld(
            "$worldName/sedimentOutflow", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { vector2i: Vector2i?, chunkSize1: Int? ->
                Chunk(
                    Vec4f2DBufferWrapper(
                        Vector2i(
                            chunkSize1!!
                        )
                    )
                )
            }, tileLoadManager
        )

        thermalOutflow1 = InfiniteChunkWorld(
            "$worldName/thermalOutflow1", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { vector2i: Vector2i?, chunkSize1: Int? ->
                Chunk(
                    Vec4f2DBufferWrapper(
                        Vector2i(
                            chunkSize1!!
                        )
                    )
                )
            }, tileLoadManager
        )
        thermalOutflow2 = InfiniteChunkWorld(
            "$worldName/thermalOutflow2", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { vector2i: Vector2i?, chunkSize1: Int? ->
                Chunk(
                    Vec4f2DBufferWrapper(
                        Vector2i(
                            chunkSize1!!
                        )
                    )
                )
            }, tileLoadManager
        )

        iterationInfo = IterableWorld("$worldName/iteration", iterationChunkSize, iterationRegionSize, tileLoadManager2)
    }

    fun unloadAll() {
        mipMappedTerrain.unloadAll()
        mipMappedWater.unloadAll()

        sediment.unloadAllRegions()
        hardness.unloadAllRegions()

        waterOutflow.unloadAllRegions()
        sedimentOutflow.unloadAllRegions()

        thermalOutflow1.unloadAllRegions()
        thermalOutflow2.unloadAllRegions()

        iterationInfo.unloadAllRegions()
    }

    fun cleanGL() {
        terrainGenerator.delete()
    }
}