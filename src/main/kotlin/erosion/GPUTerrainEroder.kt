package me.chriss99.erosion

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import glm_.vec2.Vec2i
import me.chriss99.erosion.GPUAlgorithm.Resource
import org.lwjgl.opengl.*

class GPUTerrainEroder(private val gpuAlgorithm: GPUAlgorithm, usedArea: Area) {
    init { verifyAreaSize(usedArea.size) }
    var usedArea: Area = usedArea
        set(value) {
            verifyAreaSize(value.size)

            downloadMap()
            field = value
            uploadMap()
        }

    init { uploadMap() }

    private fun verifyAreaSize(size: Vec2i) {
        require(size.allLessThanEqual(gpuAlgorithm.maxTextureSize)) { "Area cannot exceed maxTextureSize!" }
    }

    fun erode(area: Area) {
        var area = area
        require(area in usedArea) { "Area exceeds usedArea! area: $area, usedArea:$usedArea" }

        area -= usedArea.srcPos

        gpuAlgorithm.computationStages.forEach { execComputationStage(it, area) }
    }

    private fun execComputationStage(computationStage: GPUAlgorithm.ComputationStage, area: Area) {
        //correct for texture being one larger in all directions
        var area = area
        area += Vec2i(1)

        computationStage.computeProgram.use()
        GL20.glUniform2i(computationStage.srcPosUniform, area.srcPos.x, area.srcPos.y)
        GL43.glDispatchCompute(area.width, area.height, 1)
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
    }

    fun downloadMap() = gpuAlgorithm.resources.forEach(::downloadHelper)

    private fun downloadHelper(resource: Resource) {
        val bufferWrapper = Array2DBufferWrapper.of(resource.world.type, usedArea.size)
        resource.texture.downloadData(Vec2i(1), bufferWrapper)
        resource.world.writeArea(usedArea.srcPos, bufferWrapper)
    }

    fun uploadMap() {
        val area = usedArea.outset(1)
        val zero = Vec2i()

        gpuAlgorithm.resources.forEach { it.texture.uploadData(zero, it.world.readArea(area)) }
    }
}