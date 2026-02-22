package me.chriss99.erosion

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import glm_.vec2.Vec2i
import me.chriss99.erosion.GPUAlgorithm.Resource

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

        gpuAlgorithm.computationStages.forEach { it.exec(area) }
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