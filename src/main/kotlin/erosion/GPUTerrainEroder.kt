package me.chriss99.erosion

import me.chriss99.Area
import me.chriss99.Array2DBufferWrapper
import me.chriss99.Texture2D
import me.chriss99.program.ComputeProgram
import me.chriss99.worldmanagement.ErosionDataStorage
import me.chriss99.worldmanagement.InfiniteChunkWorld
import org.joml.Vector2i
import org.lwjgl.opengl.*

class GPUTerrainEroder(private val erosionDataStorage: ErosionDataStorage, maxTextureSize: Vector2i, usedArea: Area) {
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
    private val maxTextureSize = Vector2i()

    private var usedArea: Area

    private val terrainMap: Texture2D
    private val waterMap: Texture2D
    private val sedimentMap: Texture2D
    private val hardnessMap: Texture2D

    private val waterOutflowPipes: Texture2D
    private val sedimentOutflowPipes: Texture2D

    private val thermalOutflowPipes1: Texture2D
    private val thermalOutflowPipes2: Texture2D

    private val calcOutflow: ComputeProgram
    private val applyOutflowAndRest: ComputeProgram

    private val srcPosUniform1: Int
    private val srcPosUniform2: Int

    init {
        this.maxTextureSize.x = maxTextureSize.x
        this.maxTextureSize.y = maxTextureSize.y

        this.usedArea = usedArea.copy()

        //read buffer in all directions (avoids implicit out of bound reads when iterating near edges)
        val buffedMaxSize = Vector2i(maxTextureSize).add(2, 2)

        terrainMap = Texture2D(GL30.GL_R32F, buffedMaxSize)
        waterMap = Texture2D(GL30.GL_R32F, buffedMaxSize)
        sedimentMap = Texture2D(GL30.GL_R32F, buffedMaxSize)
        hardnessMap = Texture2D(GL30.GL_R32F, buffedMaxSize)

        waterOutflowPipes = Texture2D(GL30.GL_RGBA32F, buffedMaxSize)
        sedimentOutflowPipes = Texture2D(GL30.GL_RGBA32F, buffedMaxSize)

        thermalOutflowPipes1 = Texture2D(GL30.GL_RGBA32F, buffedMaxSize)
        thermalOutflowPipes2 = Texture2D(GL30.GL_RGBA32F, buffedMaxSize)


        calcOutflow = ComputeProgram("calcOutflow")
        applyOutflowAndRest = ComputeProgram("applyOutflowAndRest")

        srcPosUniform1 = calcOutflow.getUniform("srcPos")
        srcPosUniform2 = applyOutflowAndRest.getUniform("srcPos")


        terrainMap.bindUniformImage(calcOutflow.program, 0, "terrainMap", GL15.GL_READ_ONLY)
        waterMap.bindUniformImage(calcOutflow.program, 1, "waterMap", GL15.GL_READ_ONLY)
        sedimentMap.bindUniformImage(calcOutflow.program, 2, "sedimentMap", GL15.GL_READ_ONLY)
        hardnessMap.bindUniformImage(calcOutflow.program, 3, "hardnessMap", GL15.GL_READ_ONLY)
        waterOutflowPipes.bindUniformImage(calcOutflow.program, 4, "waterOutflowPipes", GL15.GL_READ_WRITE)
        sedimentOutflowPipes.bindUniformImage(calcOutflow.program, 5, "sedimentOutflowPipes", GL15.GL_WRITE_ONLY)
        thermalOutflowPipes1.bindUniformImage(calcOutflow.program, 6, "thermalOutflowPipes1", GL15.GL_WRITE_ONLY)
        thermalOutflowPipes2.bindUniformImage(calcOutflow.program, 7, "thermalOutflowPipes2", GL15.GL_WRITE_ONLY)

        terrainMap.bindUniformImage(applyOutflowAndRest.program, 0, "terrainMap", GL15.GL_READ_WRITE)
        waterMap.bindUniformImage(applyOutflowAndRest.program, 1, "waterMap", GL15.GL_READ_WRITE)
        sedimentMap.bindUniformImage(applyOutflowAndRest.program, 2, "sedimentMap", GL15.GL_READ_WRITE)
        hardnessMap.bindUniformImage(applyOutflowAndRest.program, 3, "hardnessMap", GL15.GL_READ_WRITE)
        waterOutflowPipes.bindUniformImage(applyOutflowAndRest.program, 4, "waterOutflowPipes", GL15.GL_READ_ONLY)
        sedimentOutflowPipes.bindUniformImage(applyOutflowAndRest.program, 5, "sedimentOutflowPipes", GL15.GL_READ_ONLY)
        thermalOutflowPipes1.bindUniformImage(applyOutflowAndRest.program, 6, "thermalOutflowPipes1", GL15.GL_READ_ONLY)
        thermalOutflowPipes2.bindUniformImage(applyOutflowAndRest.program, 7, "thermalOutflowPipes2", GL15.GL_READ_ONLY)



        uploadMap()
    }

    fun erode(area: Area) {
        var area = area
        require(usedArea.contains(area)) { "Area exceeds usedArea! area: $area, usedArea:$usedArea" }

        area = area.sub(usedArea.srcPos())

        execShader(calcOutflow, srcPosUniform1, area)
        execShader(applyOutflowAndRest, srcPosUniform2, area)
    }

    private fun execShader(program: ComputeProgram, srcPosUniform: Int, area: Area) {
        //correct for texture being one larger in all directions
        var area = area
        area = area.add(Vector2i(1))

        program.use()
        GL20.glUniform2i(srcPosUniform, area.srcPos().x, area.srcPos().y)
        GL43.glDispatchCompute(area.width, area.height, 1)
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
    }

    fun changeArea(area: Area) {
        require(!(area.size.x > maxTextureSize.x || area.size.y > maxTextureSize.y)) { "New area cannot exceed maxTextureSize!" }

        downloadMap()
        usedArea = area
        uploadMap()
    }

    fun downloadMap() {
        downloadHelper(terrainMap, erosionDataStorage.terrain)
        downloadHelper(waterMap, erosionDataStorage.water)
        downloadHelper(sedimentMap, erosionDataStorage.sediment)
        downloadHelper(hardnessMap, erosionDataStorage.hardness)

        downloadHelper(waterOutflowPipes, erosionDataStorage.waterOutflow)
        downloadHelper(sedimentOutflowPipes, erosionDataStorage.sedimentOutflow)

        downloadHelper(thermalOutflowPipes1, erosionDataStorage.thermalOutflow1)
        downloadHelper(thermalOutflowPipes2, erosionDataStorage.thermalOutflow2)
    }

    private fun downloadHelper(download: Texture2D, write: InfiniteChunkWorld) {
        val bufferWrapper = Array2DBufferWrapper.of(write.type, usedArea.size)
        download.downloadData(Vector2i(1), bufferWrapper)
        write.writeArea(usedArea.srcPos(), bufferWrapper)
    }

    fun uploadMap() {
        val area = usedArea.outset(1)
        val zero = Vector2i()

        terrainMap.uploadData(zero, erosionDataStorage.terrain.readArea(area))
        waterMap.uploadData(zero, erosionDataStorage.water.readArea(area))
        sedimentMap.uploadData(zero, erosionDataStorage.sediment.readArea(area))
        hardnessMap.uploadData(zero, erosionDataStorage.hardness.readArea(area))

        waterOutflowPipes.uploadData(zero, erosionDataStorage.waterOutflow.readArea(area))
        sedimentOutflowPipes.uploadData(zero, erosionDataStorage.sedimentOutflow.readArea(area))

        thermalOutflowPipes1.uploadData(zero, erosionDataStorage.thermalOutflow1.readArea(area))
        thermalOutflowPipes2.uploadData(zero, erosionDataStorage.thermalOutflow2.readArea(area))
    }

    fun delete() {
        calcOutflow.delete()
        applyOutflowAndRest.delete()

        terrainMap.delete()
        waterMap.delete()
        sedimentMap.delete()
        hardnessMap.delete()

        waterOutflowPipes.delete()
        sedimentOutflowPipes.delete()

        thermalOutflowPipes1.delete()
        thermalOutflowPipes2.delete()
    }

    fun getMaxTextureSize(): Vector2i {
        return Vector2i(maxTextureSize)
    }

    fun getUsedArea(): Area {
        return usedArea.copy()
    }
}