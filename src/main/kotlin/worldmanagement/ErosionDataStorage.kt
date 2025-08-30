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

    private val upliftGenerator = TerrainGenerator(chunkSize)

    val height: InfiniteChunkWorld
    val drainageArea: InfiniteChunkWorld

    val steepestNeighbourOffsetIndex: InfiniteChunkWorld
    val receiverHeight: InfiniteChunkWorld
    val drainageAreaCopy: InfiniteChunkWorld
    val laplacian: InfiniteChunkWorld

    val uplift: InfiniteChunkWorld

    val iterationInfo: IterableWorld

    val chunkLoadManager = OutsideSquareTLM<Region<Chunk>>(
        Util.ceilDiv(chunkRenderDistance, regionSize) +
                Util.ceilDiv(chunkLoadBufferDistance, regionSize),
        Util.floorDiv(playerPos, chunkSize))
    val loadNothingLoadManager = OutsideSquareTLM<Region<Chunk>>(0, Vec2i(0))
    val loadNothingLoadManager2 = OutsideSquareTLM<Region<IterationTile>>(0, Vec2i(0))

    init {
        height = InfiniteChunkWorld(
            "$worldName/height", Array2DBufferWrapper.Type.FLOAT, chunkSize, regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) },
            chunkLoadManager)
        drainageArea = InfiniteChunkWorld(
            "$worldName/drainageArea", Array2DBufferWrapper.Type.FLOAT, chunkSize, regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize), 1f)) },
            chunkLoadManager)

        steepestNeighbourOffsetIndex = InfiniteChunkWorld(
            "$worldName/steepestNeighbourOffsetIndex", Array2DBufferWrapper.Type.BYTE,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Byte2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )
        receiverHeight = InfiniteChunkWorld(
            "$worldName/receiverHeight", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )
        drainageAreaCopy = InfiniteChunkWorld(
            "$worldName/drainageAreaCopy", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )
        laplacian = InfiniteChunkWorld(
            "$worldName/laplacian", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) },
            loadNothingLoadManager
        )

        uplift = InfiniteChunkWorld(
            "$worldName/uplift", Array2DBufferWrapper.Type.FLOAT,
            chunkSize,
            regionSize,
            upliftGenerator::generateChunk,
            loadNothingLoadManager
        )

        iterationInfo = IterableWorld("$worldName/iteration", iterationChunkSize, iterationRegionSize, loadNothingLoadManager2)
    }

    fun manageLoad(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) {
        chunkLoadManager.radius = Util.ceilDiv(chunkRenderDistance, regionSize) +
                    Util.ceilDiv(chunkLoadBufferDistance, regionSize)
        chunkLoadManager.center = Util.floorDiv(playerPos, chunkSize*regionSize)

        height.manageLoad()
        drainageArea.manageLoad()

        steepestNeighbourOffsetIndex.manageLoad()
        receiverHeight.manageLoad()
        drainageAreaCopy.manageLoad()
        laplacian.manageLoad()

        uplift.manageLoad()

        iterationInfo.manageLoad()
    }

    fun unloadAll() {
        height.unloadAllRegions()
        drainageArea.unloadAllRegions()

        steepestNeighbourOffsetIndex.unloadAllRegions()
        receiverHeight.unloadAllRegions()
        drainageAreaCopy.unloadAllRegions()
        laplacian.unloadAllRegions()

        uplift.unloadAllRegions()

        iterationInfo.unloadAllRegions()
    }

    fun cleanGL() = upliftGenerator.delete()
}