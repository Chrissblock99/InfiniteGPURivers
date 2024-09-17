package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

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

    private final ComputeProgram combinedErosion;
    private final int stepType;
    private final int[] stepTypes;

    private final ComputeProgram initTextures;

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


        combinedErosion = new ComputeProgram("combinedErosion");


        stepTypes = new int[]{
                2,
                3,
                4
        };


        terrainMap.bindUniformImage(combinedErosion.program, 0, "terrainMap", GL_READ_WRITE);
        waterMap.bindUniformImage(combinedErosion.program, 1, "waterMap", GL_READ_WRITE);
        sedimentMap.bindUniformImage(combinedErosion.program, 2, "sedimentMap", GL_READ_WRITE);
        hardnessMap.bindUniformImage(combinedErosion.program, 3, "hardnessMap", GL_READ_WRITE);
        waterOutflowPipes.bindUniformImage(combinedErosion.program, 4, "waterOutflowPipes", GL_READ_WRITE);
        sedimentOutflowPipes.bindUniformImage(combinedErosion.program, 5, "sedimentOutflowPipes", GL_READ_WRITE);
        thermalOutflowPipes1.bindUniformImage(combinedErosion.program, 6, "thermalOutflowPipes1", GL_READ_WRITE);
        thermalOutflowPipes2.bindUniformImage(combinedErosion.program, 7, "thermalOutflowPipes2", GL_READ_WRITE);

        stepType = combinedErosion.getUniform("stepType");



        initTextures = new ComputeProgram("initTextures");

        terrainMap.bindUniformImage(initTextures.program, 0, "terrainMap", GL_WRITE_ONLY);
        waterMap.bindUniformImage(initTextures.program, 1, "waterMap", GL_WRITE_ONLY);
        sedimentMap.bindUniformImage(initTextures.program, 2, "sedimentMap", GL_WRITE_ONLY);
        hardnessMap.bindUniformImage(initTextures.program, 3, "hardnessMap", GL_WRITE_ONLY);
        waterOutflowPipes.bindUniformImage(initTextures.program, 4, "waterOutflowPipes", GL_WRITE_ONLY);

        initTextures.use();
        glDispatchCompute(width, height, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        Main.printErrors();
    }

    public void erosionSteps(int steps) {
        if (steps == 0)
            return;

        combinedErosion.use();

        execShader(1);
        for (int i = 0; i < steps; i++) {
            for (int stepType : stepTypes)
                execShader(stepType);
            if (i+1 < steps)
                execShader(6);
            else execShader(5);
        }
        execShader(7);
    }

    private void execShader(int i) {
        glUniform1i(stepType, i);
        glDispatchCompute(width, height, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
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
        combinedErosion.delete();
    }
}
