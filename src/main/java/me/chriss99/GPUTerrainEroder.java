package me.chriss99;

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
    private final Vector2i textureLimit = new Vector2i();

    private final Vector2i srcPos = new Vector2i();
    private final Vector2i size = new Vector2i();

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

    public GPUTerrainEroder(ErosionDataStorage erosionDataStorage, Vector2i srcPos, Vector2i textureLimit, Vector2i size) {
        this.erosionDataStorage = erosionDataStorage;
        this.textureLimit.x = textureLimit.x;
        this.textureLimit.y = textureLimit.y;

        this.srcPos.x = srcPos.x;
        this.srcPos.y = srcPos.y;
        this.size.x = size.x;
        this.size.y = size.y;

        //read buffer in all directions (avoids implicit out of bound reads when iterating near edges)
        Vector2i buffedLimit = new Vector2i(textureLimit).add(2, 2);

        terrainMap = new Texture2D(GL_R32F, buffedLimit.x, buffedLimit.y);
        waterMap = new Texture2D(GL_R32F, buffedLimit.x, buffedLimit.y);
        sedimentMap = new Texture2D(GL_R32F, buffedLimit.x, buffedLimit.y);
        hardnessMap = new Texture2D(GL_R32F, buffedLimit.x, buffedLimit.y);

        waterOutflowPipes = new Texture2D(GL_RGBA32F, buffedLimit.x, buffedLimit.y);
        sedimentOutflowPipes = new Texture2D(GL_RGBA32F, buffedLimit.x, buffedLimit.y);

        thermalOutflowPipes1 = new Texture2D(GL_RGBA32F, buffedLimit.x, buffedLimit.y);
        thermalOutflowPipes2 = new Texture2D(GL_RGBA32F, buffedLimit.x, buffedLimit.y);


        calcOutflow = new ComputeProgram("calcOutflow");
        applyOutflowAndRest = new ComputeProgram("applyOutflowAndRest");

        srcPosUniform1 = calcOutflow.getUniform("srcPos");
        srcPosUniform2 = calcOutflow.getUniform("srcPos");


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

    public void erosionSteps(int steps, boolean lFlat, boolean rFlat, boolean fFlat, boolean bFlat) {
        Vector2i srcPos =                new Vector2i(lFlat ? 0 : steps, bFlat ? 0 : steps);
        Vector2i endPos = new Vector2i(this.size).sub(rFlat ? 0 : steps, fFlat ? 0 : steps);

        for (int i = 0; i < steps; i++) {
            execShader(calcOutflow, srcPos, endPos);
            execShader(applyOutflowAndRest, srcPos, endPos);

            srcPos.add(lFlat ? 1 : -1, bFlat ? 1 : -1);
            endPos.sub(rFlat ? 1 : -1, fFlat ? 1 : -1);
        }
    }

    private void execShader(ComputeProgram program, Vector2i srcPos, Vector2i endPos) {
        if (endPos.x > size.x || endPos.y > size.y)
            throw new IllegalArgumentException("endPos exceeds size limitations! " + endPos + ", size:" + size);

        Vector2i size = new Vector2i(endPos).sub(srcPos);

        //correct for texture being one larger in all directions
        srcPos = new Vector2i(srcPos).add(1, 1);

        program.use();
        glUniform2i(srcPosUniform1, srcPos.x, srcPos.y);
        glUniform2i(srcPosUniform2, srcPos.x, srcPos.y);
        glDispatchCompute(size.x, size.y, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    public void changeArea(Vector2i srcPos) {
        changeArea(srcPos, size);
    }

    public void changeArea(Vector2i srcPos, Vector2i size) {
        if (size.x > textureLimit.x || size.y > textureLimit.y)
            throw new IllegalArgumentException("New size cannot exceed textureLimit!");

        downloadMap();
        this.srcPos.x = srcPos.x;
        this.srcPos.y = srcPos.y;
        this.size.x = size.x;
        this.size.y = size.y;
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
        Array2DBufferWrapper bufferWrapper = Array2DBufferWrapper.of(write.type, size.x, size.y);
        download.downloadData(1, 1, bufferWrapper);
        write.writeArea(srcPos.x, srcPos.y, bufferWrapper);
    }

    public void uploadMap() {
        Vector2i srcPos = new Vector2i(this.srcPos).sub(1, 1);
        Vector2i size = new Vector2i(this.size).add(2, 2);

        terrainMap.uploadData(0, 0, erosionDataStorage.terrain.readArea(srcPos.x, srcPos.y, size.x, size.y));
        waterMap.uploadData(0, 0, erosionDataStorage.water.readArea(srcPos.x, srcPos.y, size.x, size.y));
        sedimentMap.uploadData(0, 0, erosionDataStorage.sediment.readArea(srcPos.x, srcPos.y, size.x, size.y));
        hardnessMap.uploadData(0, 0, erosionDataStorage.hardness.readArea(srcPos.x, srcPos.y, size.x, size.y));

        waterOutflowPipes.uploadData(0, 0, erosionDataStorage.waterOutflow.readArea(srcPos.x, srcPos.y, size.x, size.y));
        sedimentOutflowPipes.uploadData(0, 0, erosionDataStorage.sedimentOutflow.readArea(srcPos.x, srcPos.y, size.x, size.y));

        thermalOutflowPipes1.uploadData(0, 0, erosionDataStorage.thermalOutflow1.readArea(srcPos.x, srcPos.y, size.x, size.y));
        thermalOutflowPipes2.uploadData(0, 0, erosionDataStorage.thermalOutflow2.readArea(srcPos.x, srcPos.y, size.x, size.y));
    }

    public void delete() {
        calcOutflow.delete();
        applyOutflowAndRest.delete();
    }

    public Vector2i getTextureLimit() {
        return new Vector2i(textureLimit);
    }

    public Vector2i getSize() {
        return new Vector2i(size);
    }

    public Vector2i getSrcPos() {
        return new Vector2i(srcPos);
    }
}
