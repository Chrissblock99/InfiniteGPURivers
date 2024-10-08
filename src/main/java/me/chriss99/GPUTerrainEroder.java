package me.chriss99;

import me.chriss99.program.ComputeProgram;
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
    private Vector2i srcPos;
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

    private final ComputeProgram addWater;
    private final ComputeProgram calcWaterAndThermalOutflow;
    private final ComputeProgram applyWaterOutflowAndErosionDeposition;
    private final ComputeProgram calcSedimentOutflow;
    private final ComputeProgram applySedimentAndThermalOutflow;
    private final ComputeProgram applySedimentThermalOutflowEvaporateAndAddWater;
    private final ComputeProgram evaporateWater;

    private final ComputeProgram[] erosionPrograms;

    public GPUTerrainEroder(ErosionDataStorage erosionDataStorage, Vector2i srcPos, int width, int height) {
        this.erosionDataStorage = erosionDataStorage;
        this.srcPos = srcPos;
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


        addWater = new ComputeProgram("addWater");
        calcWaterAndThermalOutflow = new ComputeProgram("calcWaterAndThermalOutflow");
        applyWaterOutflowAndErosionDeposition = new ComputeProgram("applyWaterOutflowAndErosionDeposition");
        calcSedimentOutflow = new ComputeProgram("calcSedimentOutflow");
        applySedimentAndThermalOutflow = new ComputeProgram("applySedimentAndThermalOutflow");
        applySedimentThermalOutflowEvaporateAndAddWater = new ComputeProgram("applySedimentThermalOutflowEvaporateAndAddWater");
        evaporateWater = new ComputeProgram("evaporateWater");


        erosionPrograms = new ComputeProgram[]{
                calcWaterAndThermalOutflow,
                applyWaterOutflowAndErosionDeposition,
                calcSedimentOutflow
        };


        waterMap.bindUniformImage(addWater.program, 1, "waterMap", GL_READ_WRITE);

        terrainMap.bindUniformImage(calcWaterAndThermalOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        waterMap.bindUniformImage(calcWaterAndThermalOutflow.program, 1, "waterMap", GL_READ_ONLY);
        hardnessMap.bindUniformImage(calcWaterAndThermalOutflow.program, 3, "hardnessMap", GL_READ_ONLY);
        waterOutflowPipes.bindUniformImage(calcWaterAndThermalOutflow.program, 4, "waterOutflowPipes", GL_READ_WRITE);
        thermalOutflowPipes1.bindUniformImage(calcWaterAndThermalOutflow.program, 6, "thermalOutflowPipes1", GL_WRITE_ONLY);
        thermalOutflowPipes2.bindUniformImage(calcWaterAndThermalOutflow.program, 7, "thermalOutflowPipes2", GL_WRITE_ONLY);

        terrainMap.bindUniformImage(applyWaterOutflowAndErosionDeposition.program, 0, "terrainMap", GL_READ_WRITE);
        waterMap.bindUniformImage(applyWaterOutflowAndErosionDeposition.program, 1, "waterMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(applyWaterOutflowAndErosionDeposition.program, 2, "sedimentMap", GL_READ_WRITE);
        hardnessMap.bindUniformImage(applyWaterOutflowAndErosionDeposition.program, 3, "hardnessMap", GL_READ_WRITE);
        waterOutflowPipes.bindUniformImage(applyWaterOutflowAndErosionDeposition.program, 4, "waterOutflowPipes", GL_READ_ONLY);

        terrainMap.bindUniformImage(calcSedimentOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        sedimentMap.bindUniformImage(calcSedimentOutflow.program, 2, "sedimentMap", GL_READ_ONLY);
        waterOutflowPipes.bindUniformImage(calcSedimentOutflow.program, 4, "waterOutflowPipes", GL_READ_ONLY);
        sedimentOutflowPipes.bindUniformImage(calcSedimentOutflow.program, 5, "sedimentOutflowPipes", GL_WRITE_ONLY);

        terrainMap.bindUniformImage(applySedimentAndThermalOutflow.program, 0, "terrainMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(applySedimentAndThermalOutflow.program, 2, "sedimentMap", GL_READ_WRITE);
        sedimentOutflowPipes.bindUniformImage(applySedimentAndThermalOutflow.program, 5, "sedimentOutflowPipes", GL_READ_ONLY);
        thermalOutflowPipes1.bindUniformImage(applySedimentAndThermalOutflow.program, 6, "thermalOutflowPipes1", GL_READ_ONLY);
        thermalOutflowPipes2.bindUniformImage(applySedimentAndThermalOutflow.program, 7, "thermalOutflowPipes2", GL_READ_ONLY);

        terrainMap.bindUniformImage(applySedimentThermalOutflowEvaporateAndAddWater.program, 0, "terrainMap", GL_READ_WRITE);
        waterMap.bindUniformImage(applySedimentThermalOutflowEvaporateAndAddWater.program, 1, "waterMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(applySedimentThermalOutflowEvaporateAndAddWater.program, 2, "sedimentMap", GL_READ_WRITE);
        sedimentOutflowPipes.bindUniformImage(applySedimentThermalOutflowEvaporateAndAddWater.program, 5, "sedimentOutflowPipes", GL_READ_ONLY);
        thermalOutflowPipes1.bindUniformImage(applySedimentThermalOutflowEvaporateAndAddWater.program, 6, "thermalOutflowPipes1", GL_READ_ONLY);
        thermalOutflowPipes2.bindUniformImage(applySedimentThermalOutflowEvaporateAndAddWater.program, 7, "thermalOutflowPipes2", GL_READ_ONLY);

        waterMap.bindUniformImage(evaporateWater.program, 1, "waterMap", GL_READ_WRITE);



        uploadMap();

        Main.printErrors();
    }

    public void erosionSteps(int steps) {
        if (steps == 0)
            return;

        execShader(addWater);
        for (int i = 0; i < steps; i++) {
            for (ComputeProgram program : erosionPrograms)
                execShader(program);
            if (i+1 < steps)
                execShader(applySedimentThermalOutflowEvaporateAndAddWater);
            else execShader(applySedimentAndThermalOutflow);
        }
        execShader(evaporateWater);
    }

    private void execShader(ComputeProgram program) {
        program.use();
        glDispatchCompute(width, height, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    public void downloadMap() {
        Float2DBufferWrapper terrain = new Float2DBufferWrapper(width, height);
        terrainMap.downloadData(0, 0, terrain);
        erosionDataStorage.terrain.writeArea(srcPos.x, srcPos.y, terrain);

        Float2DBufferWrapper water = new Float2DBufferWrapper(width, height);
        waterMap.downloadData(0, 0, water);
        erosionDataStorage.water.writeArea(srcPos.x, srcPos.y, water);

        Float2DBufferWrapper sediment = new Float2DBufferWrapper(width, height);
        sedimentMap.downloadData(0, 0, sediment);
        erosionDataStorage.sediment.writeArea(srcPos.x, srcPos.y, sediment);

        Float2DBufferWrapper hardness = new Float2DBufferWrapper(width, height);
        hardnessMap.downloadData(0, 0, hardness);
        erosionDataStorage.hardness.writeArea(srcPos.x, srcPos.y, hardness);


        Vec4f2DBufferWrapper waterOutflow = new Vec4f2DBufferWrapper(width, height);
        waterOutflowPipes.downloadData(0, 0, waterOutflow);
        erosionDataStorage.waterOutflow.writeArea(srcPos.x, srcPos.y, waterOutflow);

        Vec4f2DBufferWrapper sedimentOutflow = new Vec4f2DBufferWrapper(width, height);
        sedimentOutflowPipes.downloadData(0, 0, sedimentOutflow);
        erosionDataStorage.sedimentOutflow.writeArea(srcPos.x, srcPos.y, sedimentOutflow);


        Vec4f2DBufferWrapper thermalOutflow1 = new Vec4f2DBufferWrapper(width, height);
        thermalOutflowPipes1.downloadData(0, 0, thermalOutflow1);
        erosionDataStorage.thermalOutflow1.writeArea(srcPos.x, srcPos.y, thermalOutflow1);

        Vec4f2DBufferWrapper thermalOutflow2 = new Vec4f2DBufferWrapper(width, height);
        thermalOutflowPipes2.downloadData(0, 0, thermalOutflow2);
        erosionDataStorage.thermalOutflow2.writeArea(srcPos.x, srcPos.y, thermalOutflow2);
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
        addWater.delete();
        for (ComputeProgram program : erosionPrograms)
            program.delete();
        applySedimentAndThermalOutflow.delete();
        applySedimentThermalOutflowEvaporateAndAddWater.delete();
        evaporateWater.delete();
    }
}
