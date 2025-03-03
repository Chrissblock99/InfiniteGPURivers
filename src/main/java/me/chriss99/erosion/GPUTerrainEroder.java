package me.chriss99.erosion;

import me.chriss99.Area;
import me.chriss99.Array2DBufferWrapper;
import me.chriss99.worldmanagement.ErosionDataStorage;
import me.chriss99.Texture2D;
import me.chriss99.program.ComputeProgram;
import me.chriss99.worldmanagement.InfiniteChunkWorld;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL45.*;

public class GPUTerrainEroder {
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

    private final ErosionDataStorage erosionDataStorage;
    private final Vector2i maxTextureSize = new Vector2i();

    private Area usedArea;

    private final Texture2D terrainMap;
    private final Texture2D waterMap;
    private final Texture2D sedimentMap;
    private final Texture2D hardnessMap;

    private final Texture2D waterOutflowPipes;
    private final Texture2D sedimentOutflowPipes;

    private final Texture2D thermalOutflowPipes1;
    private final Texture2D thermalOutflowPipes2;

    private final ComputeProgram calcOutflow;
    private final ComputeProgram applyOutflowAndRest;

    private final int srcPosUniform1;
    private final int srcPosUniform2;

    public GPUTerrainEroder(ErosionDataStorage erosionDataStorage, Vector2i maxTextureSize, Area usedArea) {
        this.erosionDataStorage = erosionDataStorage;
        this.maxTextureSize.x = maxTextureSize.x;
        this.maxTextureSize.y = maxTextureSize.y;

        this.usedArea = usedArea.copy();

        //read buffer in all directions (avoids implicit out of bound reads when iterating near edges)
        Vector2i buffedMaxSize = new Vector2i(maxTextureSize).add(2, 2);

        terrainMap = new Texture2D(GL_R32F, buffedMaxSize);
        waterMap = new Texture2D(GL_R32F, buffedMaxSize);
        sedimentMap = new Texture2D(GL_R32F, buffedMaxSize);
        hardnessMap = new Texture2D(GL_R32F, buffedMaxSize);

        waterOutflowPipes = new Texture2D(GL_RGBA32F, buffedMaxSize);
        sedimentOutflowPipes = new Texture2D(GL_RGBA32F, buffedMaxSize);

        thermalOutflowPipes1 = new Texture2D(GL_RGBA32F, buffedMaxSize);
        thermalOutflowPipes2 = new Texture2D(GL_RGBA32F, buffedMaxSize);


        calcOutflow = new ComputeProgram("calcOutflow");
        applyOutflowAndRest = new ComputeProgram("applyOutflowAndRest");

        srcPosUniform1 = calcOutflow.getUniform("srcPos");
        srcPosUniform2 = applyOutflowAndRest.getUniform("srcPos");


        terrainMap.bindUniformImage(calcOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        waterMap.bindUniformImage(calcOutflow.program, 1, "waterMap", GL_READ_ONLY);
        sedimentMap.bindUniformImage(calcOutflow.program, 2, "sedimentMap", GL_READ_ONLY);
        hardnessMap.bindUniformImage(calcOutflow.program, 3, "hardnessMap", GL_READ_ONLY);
        waterOutflowPipes.bindUniformImage(calcOutflow.program, 4, "waterOutflowPipes", GL_READ_WRITE);
        sedimentOutflowPipes.bindUniformImage(calcOutflow.program, 5, "sedimentOutflowPipes", GL_WRITE_ONLY);
        thermalOutflowPipes1.bindUniformImage(calcOutflow.program, 6, "thermalOutflowPipes1", GL_WRITE_ONLY);
        thermalOutflowPipes2.bindUniformImage(calcOutflow.program, 7, "thermalOutflowPipes2", GL_WRITE_ONLY);

        terrainMap.bindUniformImage(applyOutflowAndRest.program, 0, "terrainMap", GL_READ_WRITE);
        waterMap.bindUniformImage(applyOutflowAndRest.program, 1, "waterMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(applyOutflowAndRest.program, 2, "sedimentMap", GL_READ_WRITE);
        hardnessMap.bindUniformImage(applyOutflowAndRest.program, 3, "hardnessMap", GL_READ_WRITE);
        waterOutflowPipes.bindUniformImage(applyOutflowAndRest.program, 4, "waterOutflowPipes", GL_READ_ONLY);
        sedimentOutflowPipes.bindUniformImage(applyOutflowAndRest.program, 5, "sedimentOutflowPipes", GL_READ_ONLY);
        thermalOutflowPipes1.bindUniformImage(applyOutflowAndRest.program, 6, "thermalOutflowPipes1", GL_READ_ONLY);
        thermalOutflowPipes2.bindUniformImage(applyOutflowAndRest.program, 7, "thermalOutflowPipes2", GL_READ_ONLY);



        uploadMap();
    }

    public void erode(Area area) {
        if (!usedArea.contains(area))
            throw new IllegalArgumentException("Area exceeds usedArea! area: " + area + ", usedArea:" + usedArea);

        area = area.sub(usedArea.srcPos());

        execShader(calcOutflow, area);
        execShader(applyOutflowAndRest, area);
    }

    private void execShader(ComputeProgram program, Area area) {
        //correct for texture being one larger in all directions
        area = area.add(new Vector2i(1));

        program.use();
        glUniform2i(srcPosUniform1, area.srcPos().x, area.srcPos().y);
        glUniform2i(srcPosUniform2, area.srcPos().x, area.srcPos().y);
        glDispatchCompute(area.getWidth(), area.getHeight(), 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    public void changeArea(Area area) {
        if (area.getSize().x > maxTextureSize.x || area.getSize().y > maxTextureSize.y)
            throw new IllegalArgumentException("New area cannot exceed maxTextureSize!");

        downloadMap();
        usedArea = area;
        uploadMap();
    }

    public void downloadMap() {
        downloadHelper(terrainMap, erosionDataStorage.terrain);
        downloadHelper(waterMap, erosionDataStorage.water);
        downloadHelper(sedimentMap, erosionDataStorage.sediment);
        downloadHelper(hardnessMap, erosionDataStorage.hardness);

        downloadHelper(waterOutflowPipes, erosionDataStorage.waterOutflow);
        downloadHelper(sedimentOutflowPipes, erosionDataStorage.sedimentOutflow);

        downloadHelper(thermalOutflowPipes1, erosionDataStorage.thermalOutflow1);
        downloadHelper(thermalOutflowPipes2, erosionDataStorage.thermalOutflow2);
    }

    private void downloadHelper(Texture2D download, InfiniteChunkWorld write) {
        Array2DBufferWrapper bufferWrapper = Array2DBufferWrapper.of(write.type, usedArea.getSize());
        download.downloadData(new Vector2i(1), bufferWrapper);
        write.writeArea(usedArea.srcPos(), bufferWrapper);
    }

    public void uploadMap() {
        Area area = usedArea.outset(1);
        Vector2i zero = new Vector2i();

        terrainMap.uploadData(zero, erosionDataStorage.terrain.readArea(area));
        waterMap.uploadData(zero, erosionDataStorage.water.readArea(area));
        sedimentMap.uploadData(zero, erosionDataStorage.sediment.readArea(area));
        hardnessMap.uploadData(zero, erosionDataStorage.hardness.readArea(area));

        waterOutflowPipes.uploadData(zero, erosionDataStorage.waterOutflow.readArea(area));
        sedimentOutflowPipes.uploadData(zero, erosionDataStorage.sedimentOutflow.readArea(area));

        thermalOutflowPipes1.uploadData(zero, erosionDataStorage.thermalOutflow1.readArea(area));
        thermalOutflowPipes2.uploadData(zero, erosionDataStorage.thermalOutflow2.readArea(area));
    }

    public void delete() {
        calcOutflow.delete();
        applyOutflowAndRest.delete();
    }

    public Vector2i getMaxTextureSize() {
        return new Vector2i(maxTextureSize);
    }

    public Area getUsedArea() {
        return usedArea.copy();
    }
}
