package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;

public class GPUTerrainEroder {
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
    private final ComputeProgram calcWaterOutflow;
    private final ComputeProgram applyWaterOutflow;
    private final ComputeProgram calcThermalOutflow;
    private final ComputeProgram erosionDeposition;
    private final ComputeProgram calcSedimentOutflow;
    private final ComputeProgram applySedimentAndThermalOutflow;
    private final ComputeProgram evaporateWater;

    private final ComputeProgram initTextures;
    private final ComputeProgram[] erosionPrograms;

    public GPUTerrainEroder(int width, int height) {
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
        calcWaterOutflow = new ComputeProgram("calcWaterOutflow");
        applyWaterOutflow = new ComputeProgram("applyWaterOutflow");
        calcThermalOutflow = new ComputeProgram("calcThermalOutflow");
        erosionDeposition = new ComputeProgram("erosionDeposition");
        calcSedimentOutflow = new ComputeProgram("calcSedimentOutflow");
        applySedimentAndThermalOutflow = new ComputeProgram("applySedimentAndThermalOutflow");
        evaporateWater = new ComputeProgram("evaporateWater");


        erosionPrograms = new ComputeProgram[]{
                addWater,
                calcWaterOutflow,
                applyWaterOutflow,
                calcThermalOutflow,
                erosionDeposition,
                calcSedimentOutflow,
                applySedimentAndThermalOutflow,
                evaporateWater
        };


        waterMap.bindUniformImage(addWater.program, 1, "waterMap", GL_READ_WRITE);

        terrainMap.bindUniformImage(calcWaterOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        waterMap.bindUniformImage(calcWaterOutflow.program, 1, "waterMap", GL_READ_ONLY);
        waterOutflowPipes.bindUniformImage(calcWaterOutflow.program, 4, "waterOutflowPipes", GL_READ_WRITE);

        waterMap.bindUniformImage(applyWaterOutflow.program, 1, "waterMap", GL_READ_WRITE);
        waterOutflowPipes.bindUniformImage(applyWaterOutflow.program, 4, "waterOutflowPipes", GL_READ_ONLY);

        terrainMap.bindUniformImage(calcThermalOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        hardnessMap.bindUniformImage(calcThermalOutflow.program, 3, "hardnessMap", GL_READ_ONLY);
        thermalOutflowPipes1.bindUniformImage(calcThermalOutflow.program, 6, "thermalOutflowPipes1", GL_WRITE_ONLY);
        thermalOutflowPipes2.bindUniformImage(calcThermalOutflow.program, 7, "thermalOutflowPipes2", GL_WRITE_ONLY);

        terrainMap.bindUniformImage(erosionDeposition.program, 0, "terrainMap", GL_READ_WRITE);
        waterMap.bindUniformImage(erosionDeposition.program, 1, "waterMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(erosionDeposition.program, 2, "sedimentMap", GL_READ_WRITE);
        hardnessMap.bindUniformImage(erosionDeposition.program, 3, "hardnessMap", GL_READ_WRITE);
        waterOutflowPipes.bindUniformImage(erosionDeposition.program, 4, "waterOutflowPipes", GL_READ_ONLY);

        terrainMap.bindUniformImage(calcSedimentOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        sedimentMap.bindUniformImage(calcSedimentOutflow.program, 2, "sedimentMap", GL_READ_ONLY);
        waterOutflowPipes.bindUniformImage(calcSedimentOutflow.program, 4, "waterOutflowPipes", GL_READ_ONLY);
        sedimentOutflowPipes.bindUniformImage(calcSedimentOutflow.program, 5, "sedimentOutflowPipes", GL_WRITE_ONLY);

        terrainMap.bindUniformImage(applySedimentAndThermalOutflow.program, 0, "terrainMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(applySedimentAndThermalOutflow.program, 2, "sedimentMap", GL_READ_WRITE);
        sedimentOutflowPipes.bindUniformImage(applySedimentAndThermalOutflow.program, 5, "sedimentOutflowPipes", GL_READ_ONLY);
        thermalOutflowPipes1.bindUniformImage(applySedimentAndThermalOutflow.program, 6, "thermalOutflowPipes1", GL_READ_ONLY);
        thermalOutflowPipes2.bindUniformImage(applySedimentAndThermalOutflow.program, 7, "thermalOutflowPipes2", GL_READ_ONLY);

        waterMap.bindUniformImage(evaporateWater.program, 1, "waterMap", GL_READ_WRITE);



        initTextures = new ComputeProgram("initTextures");

        terrainMap.bindUniformImage(initTextures.program, 0, "terrainMap", GL_WRITE_ONLY);
        waterMap.bindUniformImage(initTextures.program, 1, "waterMap", GL_WRITE_ONLY);
        sedimentMap.bindUniformImage(initTextures.program, 2, "sedimentMap", GL_WRITE_ONLY);
        hardnessMap.bindUniformImage(initTextures.program, 3, "hardnessMap", GL_WRITE_ONLY);
        waterOutflowPipes.bindUniformImage(initTextures.program, 4, "waterOutflowPipes", GL_WRITE_ONLY);

        glUseProgram(initTextures.program);
        glDispatchCompute(width, height, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        Main.printErrors();
    }

    public void erosionStep() {
        for (ComputeProgram program : erosionPrograms) {
            glUseProgram(program.program);
            glDispatchCompute(width, height, 1);
            glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        }
    }

    public double[][][] downloadMap() {
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width*height*4);
        terrainMap.downloadFullData(GL_RED, GL_FLOAT, byteBuffer);
        double[][] terrainMap = new double[width][height];

        for (int i = 0; i < width*height; i++) {
            int x = i % width;
            int z = (i - x) / width;
            terrainMap[x][z] = byteBuffer.getFloat(i*4);
        }

        waterMap.downloadFullData(GL_RED, GL_FLOAT, byteBuffer);
        double[][] addedMap = new double[width][height];

        for (int i = 0; i < width*height; i++) {
            int x = i % width;
            int z = (i - x) / width;
            addedMap[x][z] = byteBuffer.getFloat(i*4) + terrainMap[x][z];
        }

        return new double[][][]{terrainMap, addedMap};
    }

    public double[][][] downloadWaterOutflow() {
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width*height*4*4);
        waterOutflowPipes.downloadFullData(GL_RGBA, GL_FLOAT, byteBuffer);
        double[][][] waterOutflow = new double[width][height][4];

        for (int i = 0; i < width*height; i++) {
            int x = i % width;
            int z = (i - x) / width;
            waterOutflow[x][z][0] = byteBuffer.getFloat(i*4*4);
            waterOutflow[x][z][1] = byteBuffer.getFloat(i*4*4 + 4);
            waterOutflow[x][z][2] = byteBuffer.getFloat(i*4*4 + 8);
            waterOutflow[x][z][3] = byteBuffer.getFloat(i*4*4 + 12);
        }

        return waterOutflow;
    }

    public void delete() {
        initTextures.delete();
        for (ComputeProgram program : erosionPrograms)
            program.delete();
    }
}
