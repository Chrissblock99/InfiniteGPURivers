package me.chriss99.erosion

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import me.chriss99.Texture2D
import me.chriss99.program.ComputeProgram
import me.chriss99.worldmanagement.ErosionDataStorage
import me.chriss99.worldmanagement.InfiniteChunkWorld
import glm_.vec2.Vec2i
import org.lwjgl.opengl.*

class GPUTerrainEroder(private val erosionDataStorage: ErosionDataStorage, val maxTextureSize: Vec2i, usedArea: Area) {
    /*
    double deltaT = 0.02; //[0;0.05]

    double rainRate = 0.012; //[0;0.05]
    double evaporationRate = 0.015; //[0;0.05]
    double waterFlowMultiplier = 1; //[0.1;2]
    double sedimentCapacityMultiplier = 1; //[0.1;3]
    double thermalErosionRate = 0.75; //[0;3]
    double soilSuspensionRate = 0.5; //[0.1;2]
    double sedimentDepositionRate = 1; //[0.1;3]
    double sedimentSofteningRate = 5; //[0;10]
    double maxErosionDepth = 10; //[0;40]
    double talusAngleTangentCoeff = 0.8; //[0;1]
    double talusAngleTangentBias = 0.1; //[0;1]
    double minimumHardness = 0.25; //[0;1]
    double voidSediment = 0.3; //[0;1]
    */
    init { verifyAreaSize(usedArea.size) }
    var usedArea: Area = usedArea
        set(value) {
            verifyAreaSize(value.size)

            downloadMap()
            field = value
            uploadMap()
        }

    private val heightMap: Texture2D
    private val waterMap: Texture2D

    private val steepestNeighbourOffsetIndexMap: Texture2D
    private val receiverHeightMap: Texture2D
    private val drainageAreaCopyMap: Texture2D
    private val laplacianMap: Texture2D

    private val upliftMap: Texture2D

    private val calcSteepestAndCopy: ComputeProgram
    private val calcDrainageAndErode: ComputeProgram

    private val srcPosUniform1: Int
    private val srcPosUniform2: Int

    init {
        //read buffer in all directions (avoids implicit out of bound reads when iterating near edges)
        val buffedMaxSize = maxTextureSize + 2

        heightMap = Texture2D(GL30.GL_R32F, buffedMaxSize)
        waterMap = Texture2D(GL30.GL_R32F, buffedMaxSize)

        steepestNeighbourOffsetIndexMap = Texture2D(GL30.GL_R8I, buffedMaxSize)
        receiverHeightMap = Texture2D(GL30.GL_R32F, buffedMaxSize)
        drainageAreaCopyMap = Texture2D(GL30.GL_R32F, buffedMaxSize)
        laplacianMap = Texture2D(GL30.GL_R32F, buffedMaxSize)

        upliftMap = Texture2D(GL30.GL_R32F, buffedMaxSize)


        calcSteepestAndCopy = ComputeProgram("calcSteepestAndCopy")
        calcDrainageAndErode = ComputeProgram("calcDrainageAndErode")

        srcPosUniform1 = calcSteepestAndCopy.getUniform("srcPos")
        srcPosUniform2 = calcDrainageAndErode.getUniform("srcPos")


        heightMap.bindUniformImage(calcSteepestAndCopy.program, 0, "heightMap", GL15.GL_READ_ONLY)
        waterMap.bindUniformImage(calcSteepestAndCopy.program, 1, "waterMap", GL15.GL_READ_ONLY)
        steepestNeighbourOffsetIndexMap.bindUniformImage(calcSteepestAndCopy.program, 2, "steepestNeighbourOffsetIndexMap", GL15.GL_WRITE_ONLY)
        receiverHeightMap.bindUniformImage(calcSteepestAndCopy.program, 3, "receiverHeightMap", GL15.GL_WRITE_ONLY)
        drainageAreaCopyMap.bindUniformImage(calcSteepestAndCopy.program, 4, "drainageAreaCopyMap", GL15.GL_WRITE_ONLY)
        laplacianMap.bindUniformImage(calcSteepestAndCopy.program, 5, "laplacianMap", GL15.GL_WRITE_ONLY)

        heightMap.bindUniformImage(calcDrainageAndErode.program, 0, "terrainMap", GL15.GL_READ_WRITE)
        waterMap.bindUniformImage(calcDrainageAndErode.program, 1, "waterMap", GL15.GL_READ_WRITE)
        steepestNeighbourOffsetIndexMap.bindUniformImage(calcDrainageAndErode.program, 2, "steepestNeighbourOffsetIndexMap", GL15.GL_READ_ONLY)
        receiverHeightMap.bindUniformImage(calcDrainageAndErode.program, 3, "receiverHeightMap", GL15.GL_READ_ONLY)
        drainageAreaCopyMap.bindUniformImage(calcDrainageAndErode.program, 4, "drainageAreaCopyMap", GL15.GL_READ_ONLY)
        laplacianMap.bindUniformImage(calcSteepestAndCopy.program, 5, "laplacianMap", GL15.GL_READ_ONLY)
        upliftMap.bindUniformImage(calcDrainageAndErode.program, 6, "upliftMap", GL15.GL_READ_ONLY)



        uploadMap()
    }

    private fun verifyAreaSize(size: Vec2i) {
        require(size.allLessThanEqual(maxTextureSize)) { "Area cannot exceed maxTextureSize!" }
    }

    fun erode(area: Area) {
        var area = area
        require(area in usedArea) { "Area exceeds usedArea! area: $area, usedArea:$usedArea" }

        area -= usedArea.srcPos

        execShader(calcSteepestAndCopy, srcPosUniform1, area)
        execShader(calcDrainageAndErode, srcPosUniform2, area)
    }

    private fun execShader(program: ComputeProgram, srcPosUniform: Int, area: Area) {
        //correct for texture being one larger in all directions
        var area = area
        area += Vec2i(1)

        program.use()
        GL20.glUniform2i(srcPosUniform, area.srcPos.x, area.srcPos.y)
        GL43.glDispatchCompute(area.width, area.height, 1)
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
    }

    fun downloadMap() {
        downloadHelper(heightMap, erosionDataStorage.height)
        downloadHelper(waterMap, erosionDataStorage.drainageArea)

        downloadHelper(steepestNeighbourOffsetIndexMap, erosionDataStorage.steepestNeighbourOffsetIndex)
        downloadHelper(receiverHeightMap, erosionDataStorage.receiverHeight)
        downloadHelper(drainageAreaCopyMap, erosionDataStorage.drainageAreaCopy)
        downloadHelper(laplacianMap, erosionDataStorage.laplacian)
    }

    private fun downloadHelper(download: Texture2D, write: InfiniteChunkWorld) {
        val bufferWrapper = Array2DBufferWrapper.of(write.type, usedArea.size)
        download.downloadData(Vec2i(1), bufferWrapper)
        write.writeArea(usedArea.srcPos, bufferWrapper)
    }

    fun uploadMap() {
        val area = usedArea.outset(1)
        val zero = Vec2i()

        heightMap.uploadData(zero, erosionDataStorage.height.readArea(area))
        waterMap.uploadData(zero, erosionDataStorage.drainageArea.readArea(area))

        steepestNeighbourOffsetIndexMap.uploadData(zero, erosionDataStorage.steepestNeighbourOffsetIndex.readArea(area))
        receiverHeightMap.uploadData(zero, erosionDataStorage.receiverHeight.readArea(area))
        drainageAreaCopyMap.uploadData(zero, erosionDataStorage.drainageAreaCopy.readArea(area))
        laplacianMap.uploadData(zero, erosionDataStorage.laplacian.readArea(area))

        upliftMap.uploadData(zero, erosionDataStorage.uplift.readArea(area))
    }

    fun delete() {
        calcSteepestAndCopy.delete()
        calcDrainageAndErode.delete()

        heightMap.delete()
        waterMap.delete()

        steepestNeighbourOffsetIndexMap.delete()
        receiverHeightMap.delete()
        drainageAreaCopyMap.delete()
        laplacianMap.delete()

        upliftMap.delete()
    }
}