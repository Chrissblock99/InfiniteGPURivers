package me.chriss99.worldmanagement

import me.chriss99.*
import me.chriss99.worldmanagement.iteration.IterableWorld
import me.chriss99.worldmanagement.iteration.IterationTile
import glm_.vec2.Vec2i
import me.chriss99.util.Util

class ErosionDataStorage(worldName: String, chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) {
    val chunkSize = 64
    val regionSize = 10
    val iterationChunkSize = 64
    val iterationRegionSize = 10

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

    val chunkLoadManager = OutsideSquareTLM<Region<Chunk>>(
        Util.ceilDiv(chunkRenderDistance, regionSize) +
                Util.ceilDiv(chunkLoadBufferDistance, regionSize),
        Util.floorDiv(playerPos, chunkSize))
    val loadNothingLoadManager = OutsideSquareTLM<Region<Chunk>>(0, Vec2i(0))
    val loadNothingLoadManager2 = OutsideSquareTLM<Region<IterationTile>>(0, Vec2i(0))

    init {
        mipMappedTerrain = MipMappedInfiniteChunkWorld(
            "$worldName/terrain", chunkSize, regionSize,
            terrainGenerator::generateChunk,
            { _ -> chunkLoadManager })
        mipMappedWater = MipMappedInfiniteChunkWorld(
            "$worldName/water", chunkSize, regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) },
            { _ -> chunkLoadManager })

        terrain = mipMappedTerrain.getMipMapLevel(0)
        water = mipMappedWater.getMipMapLevel(0)
        sediment = InfiniteChunkWorld(
            "$worldName/sediment", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )
        hardness = InfiniteChunkWorld(
            "$worldName/hardness", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize), 1f)) },
            loadNothingLoadManager
        )

        waterOutflow = InfiniteChunkWorld(
            "$worldName/waterOutflow", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Vec4f2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )
        sedimentOutflow = InfiniteChunkWorld(
            "$worldName/sedimentOutflow", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Vec4f2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )

        thermalOutflow1 = InfiniteChunkWorld(
            "$worldName/thermalOutflow1", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Vec4f2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )
        thermalOutflow2 = InfiniteChunkWorld(
            "$worldName/thermalOutflow2", Array2DBufferWrapper.Type.VEC4F,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Vec4f2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )

        iterationInfo = IterableWorld("$worldName/iteration", iterationChunkSize, iterationRegionSize, loadNothingLoadManager2)
    }

    fun manageLoad(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, iterationRenderDistance: Int, playerPos: Vec2i) {
        chunkLoadManager.radius = Util.ceilDiv(chunkRenderDistance, regionSize) +
                    Util.ceilDiv(chunkLoadBufferDistance, regionSize)
        chunkLoadManager.center = Util.floorDiv(playerPos, chunkSize*regionSize)

        mipMappedTerrain.manageLoad()
        mipMappedWater.manageLoad()

        sediment.manageLoad()
        hardness.manageLoad()

        waterOutflow.manageLoad()
        sedimentOutflow.manageLoad()

        thermalOutflow1.manageLoad()
        thermalOutflow2.manageLoad()

        iterationInfo.manageLoad()
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

    fun cleanGL() = terrainGenerator.delete()
}