package me.chriss99

import glm_.vec2.Vec2i
import me.chriss99.erosion.GPUAlgorithm
import me.chriss99.util.Util.ceilDiv
import me.chriss99.util.Util.floorDiv
import me.chriss99.worldmanagement.Chunk
import me.chriss99.worldmanagement.Region

class StreamPower(worldName: String, maxTextureSize: Vec2i, chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) : GPUAlgorithm(worldName, maxTextureSize) {
    private val upliftGenerator = HeightMapGenerator("genHeightMap", 8, chunkSize)


    val chunkLoadManager = OutsideSquareTLM<Region<Chunk>>(
        (chunkRenderDistance ceilDiv regionSize) +
                (chunkLoadBufferDistance ceilDiv regionSize),
        (playerPos floorDiv chunkSize))


    val height = Resource("height", Array2DBufferWrapper.Type.FLOAT, chunkLoadManager)
    val drainageArea = Resource("drainageArea", Array2DBufferWrapper.Type.FLOAT)
    { _, chunkSize -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize), 1f)) }

    val steepestNeighbourOffsetIndex = Resource("steepestNeighbourOffsetIndex", Array2DBufferWrapper.Type.BYTE)
    val receiverHeight = Resource("receiverHeight", Array2DBufferWrapper.Type.FLOAT)
    val drainageAreaCopy = Resource("drainageAreaCopy", Array2DBufferWrapper.Type.FLOAT)
    val laplacian = Resource("laplacian", Array2DBufferWrapper.Type.FLOAT)

    val uplift = Resource("uplift", Array2DBufferWrapper.Type.FLOAT, chunkGenerator = upliftGenerator::generateChunk)

    init {
        ComputationStage("calcSteepestAndCopy", mapOf(
            height to Access.READ_ONLY,
            drainageArea to Access.READ_ONLY,
            steepestNeighbourOffsetIndex to Access.WRITE_ONLY,
            receiverHeight to Access.WRITE_ONLY,
            drainageAreaCopy to Access.WRITE_ONLY,
            laplacian to Access.WRITE_ONLY
        ))
        ComputationStage("calcDrainageAndErode", mapOf(
            height to Access.READ_WRITE,
            drainageArea to Access.READ_WRITE,
            steepestNeighbourOffsetIndex to Access.READ_ONLY,
            receiverHeight to Access.READ_ONLY,
            drainageAreaCopy to Access.READ_ONLY,
            laplacian to Access.READ_ONLY,
            uplift to Access.READ_ONLY
        ))
    }

    override fun updateLoadManagers(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) {
        chunkLoadManager.radius = (chunkRenderDistance ceilDiv regionSize) +
                (chunkLoadBufferDistance ceilDiv regionSize)
        chunkLoadManager.center = (playerPos floorDiv (chunkSize*regionSize))
    }

    override fun cleanGL() {
        upliftGenerator.delete()
    }
}