package me.chriss99;

import me.chriss99.program.ComputeProgram;
import me.chriss99.worldmanagement.InfiniteWorld;
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
    private final Vector2i srcPos = new Vector2i();
    private final int width;
    private final int height;

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

    public GPUTerrainEroder(ErosionDataStorage erosionDataStorage, Vector2i srcPos, int width, int height) {
        this.erosionDataStorage = erosionDataStorage;
        this.srcPos.x = srcPos.x;
        this.srcPos.y = srcPos.y;
        this.width = width;
        this.height = height;

        terrainMap = new Texture2D(GL_R32F, width, height);
        waterMap = new Texture2D(GL_R32F, width, height);
        sedimentMap = new Texture2D(GL_R32F, width, height);
        hardnessMap = new Texture2D(GL_R32F, width, height);

        waterOutflowPipes = new Texture2D(GL_RGBA32F, width, height);
        sedimentOutflowPipes = new Texture2D(GL_RGBA32F, width, height);

        thermalOutflowPipes1 = new Texture2D(GL_RGBA32F, width, height);
        thermalOutflowPipes2 = new Texture2D(GL_RGBA32F, width, height);


        calcOutflow = new ComputeProgram("calcOutflow");
        applyOutflowAndRest = new ComputeProgram("applyOutflowAndRest");


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

        Main.printErrors();
    }

    public void erosionSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            execShader(calcOutflow);
            execShader(applyOutflowAndRest);
        }
    }

    private void execShader(ComputeProgram program) {
        program.use();
        glDispatchCompute(width, height, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    public void changeSrcPos(Vector2i srcPos) {
        downloadMap();
        this.srcPos.x = srcPos.x;
        this.srcPos.y = srcPos.y;
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

    private void downloadHelper(Texture2D download, InfiniteWorld write) {
        Array2DBufferWrapper bufferWrapper = new Array2DBufferWrapper(write.format, write.type, width, height);
        download.downloadData(0, 0, bufferWrapper);
        write.writeArea(srcPos.x, srcPos.y, bufferWrapper);
    }

    public void uploadMap() {
        terrainMap.uploadData(0, 0, erosionDataStorage.terrain.readArea(srcPos.x, srcPos.y, width, height));
        waterMap.uploadData(0, 0, erosionDataStorage.water.readArea(srcPos.x, srcPos.y, width, height));
        sedimentMap.uploadData(0, 0, erosionDataStorage.sediment.readArea(srcPos.x, srcPos.y, width, height));
        hardnessMap.uploadData(0, 0, erosionDataStorage.hardness.readArea(srcPos.x, srcPos.y, width, height));

        waterOutflowPipes.uploadData(0, 0, erosionDataStorage.waterOutflow.readArea(srcPos.x, srcPos.y, width, height));
        sedimentOutflowPipes.uploadData(0, 0, erosionDataStorage.sedimentOutflow.readArea(srcPos.x, srcPos.y, width, height));

        thermalOutflowPipes1.uploadData(0, 0, erosionDataStorage.thermalOutflow1.readArea(srcPos.x, srcPos.y, width, height));
        thermalOutflowPipes2.uploadData(0, 0, erosionDataStorage.thermalOutflow2.readArea(srcPos.x, srcPos.y, width, height));
    }

    public void delete() {
        calcOutflow.delete();
        applyOutflowAndRest.delete();
    }
}
