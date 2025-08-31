package me.chriss99

import glm_.vec2.Vec2i
import me.chriss99.erosion.GPUAlgorithm
import me.chriss99.util.Util
import me.chriss99.worldmanagement.Chunk
import me.chriss99.worldmanagement.Region

class FluidSim(worldName: String, maxTextureSize: Vec2i, chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) : GPUAlgorithm(worldName, maxTextureSize) {
    private val terrainGenerator = TerrainGenerator(chunkSize)

    val chunkLoadManager = OutsideSquareTLM<Region<Chunk>>(
        Util.ceilDiv(chunkRenderDistance, regionSize) +
                Util.ceilDiv(chunkLoadBufferDistance, regionSize),
        Util.floorDiv(playerPos, chunkSize))

    val terrain = Resource("terrain", Array2DBufferWrapper.Type.FLOAT, chunkLoadManager, terrainGenerator::generateChunk)
    val water = Resource("water", Array2DBufferWrapper.Type.FLOAT, chunkLoadManager)
    val sediment = Resource("sediment", Array2DBufferWrapper.Type.FLOAT)
    val hardness = Resource("hardness", Array2DBufferWrapper.Type.FLOAT)

    val waterOutflowPipes = Resource("waterOutflowPipes", Array2DBufferWrapper.Type.VEC4F)
    val sedimentOutflowPipes = Resource("sedimentOutflowPipes", Array2DBufferWrapper.Type.VEC4F)

    val thermalOutflowPipes1 = Resource("thermalOutflowPipes1", Array2DBufferWrapper.Type.VEC4F)
    val thermalOutflowPipes2 = Resource("thermalOutflowPipes2", Array2DBufferWrapper.Type.VEC4F)

    init {
        ComputationStage("calcOutflow", mapOf(
            terrain to Access.READ_ONLY,
            water to Access.READ_ONLY,
            sediment to Access.READ_ONLY,
            hardness to Access.READ_ONLY,
            waterOutflowPipes to Access.READ_WRITE,
            sedimentOutflowPipes to Access.WRITE_ONLY,
            thermalOutflowPipes1 to Access.WRITE_ONLY,
            thermalOutflowPipes2 to Access.WRITE_ONLY
        ))
        ComputationStage("applyOutflowAndRest", mapOf(
            terrain to Access.READ_WRITE,
            water to Access.READ_WRITE,
            sediment to Access.READ_WRITE,
            hardness to Access.READ_WRITE,
            waterOutflowPipes to Access.READ_ONLY,
            sedimentOutflowPipes to Access.READ_ONLY,
            thermalOutflowPipes1 to Access.READ_ONLY,
            thermalOutflowPipes2 to Access.READ_ONLY
        ))
    }

    override fun updateLoadManagers(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) {
        chunkLoadManager.radius = Util.ceilDiv(chunkRenderDistance, regionSize) +
                Util.ceilDiv(chunkLoadBufferDistance, regionSize)
        chunkLoadManager.center = Util.floorDiv(playerPos, chunkSize*regionSize)
    }

    override fun cleanGL() {
        terrainGenerator.delete()
    }
}