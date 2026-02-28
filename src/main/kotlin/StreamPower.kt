package me.chriss99

import glm_.vec2.Vec2i
import me.chriss99.erosion.GPUAlgorithm
import me.chriss99.util.Util.ceilDiv
import me.chriss99.util.Util.floorDiv
import me.chriss99.worldmanagement.Chunk
import me.chriss99.worldmanagement.Region

class StreamPower(worldName: String, maxTextureSize: Vec2i, chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) : GPUAlgorithm(worldName, maxTextureSize, 1) {
	val bedrockGenerator = HeightMapGenerator("bedrockInit", 10, 64)
	val upliftGenerator = HeightMapGenerator("upliftInit", 11, 64)

	val chunkLoadManager = OutsideSquareTLM<Region<Chunk>>(
		(chunkRenderDistance ceilDiv regionSize) +
				(chunkLoadBufferDistance ceilDiv regionSize),
		(playerPos floorDiv chunkSize))
	
	val bedrock = Resource("bedrock", Array2DBufferWrapper.Type.FLOAT, chunkLoadManager, bedrockGenerator::generateChunk)
	val tempBedrock = Resource("tempBedrock", Array2DBufferWrapper.Type.FLOAT)

	val stream = Resource("stream", Array2DBufferWrapper.Type.FLOAT)
	val tempStream = Resource("tempStream", Array2DBufferWrapper.Type.FLOAT)

	val uplift = Resource("uplift", Array2DBufferWrapper.Type.FLOAT, chunkGenerator = upliftGenerator::generateChunk)

	val steepest = Resource("steepest", Array2DBufferWrapper.Type.BYTE)

	init {
		ComputationStage("spe_shader_precalc", mapOf(
			bedrock to Access.READ_ONLY,
			steepest to Access.WRITE_ONLY
		))
		ComputationStage("spe_shader", mapOf(
			bedrock to Access.READ_ONLY,
			stream to Access.READ_ONLY,
			tempBedrock to Access.WRITE_ONLY,
			tempStream to Access.WRITE_ONLY,
			uplift to Access.READ_ONLY,
			steepest to Access.READ_ONLY
		))
		// dual buffering
		ComputationStage("spe_shader_precalc2", mapOf(
			tempBedrock to Access.READ_ONLY,
			steepest to Access.WRITE_ONLY
		))
		ComputationStage("spe_shader2", mapOf(
			bedrock to Access.WRITE_ONLY,
			stream to Access.WRITE_ONLY,
			tempBedrock to Access.READ_ONLY,
			tempStream to Access.READ_ONLY,
			uplift to Access.READ_ONLY,
			steepest to Access.READ_ONLY
		))
	}

	override fun updateLoadManagers(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) {
        chunkLoadManager.radius = (chunkRenderDistance ceilDiv regionSize) +
                (chunkLoadBufferDistance ceilDiv regionSize)
        chunkLoadManager.center = (playerPos floorDiv (chunkSize*regionSize))
    }

	override fun cleanGL() {
        bedrockGenerator.delete()
		upliftGenerator.delete()
	}
}