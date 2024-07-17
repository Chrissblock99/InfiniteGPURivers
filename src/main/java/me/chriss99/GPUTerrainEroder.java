package me.chriss99;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;

public class GPUTerrainEroder {
    private final int width;
    private final int height;

    private final Texture terrainMap;
    private final Texture waterMap;
    private final Texture sedimentMap;
    private final Texture hardnessMap;

    private final Texture waterOutflowPipes;
    private final Texture thermalOutflowPipes;
    private final Texture velocityField;
    private final Texture sedimentOutflowPipes;

    private final ComputeProgram addWater;
    private final ComputeProgram calcWaterOutflow;
    //private final ComputeProgram applyWaterOutflow;
    private final ComputeProgram evaporateWater;

    private final ComputeProgram[] erosionPrograms;

    public GPUTerrainEroder(int width, int height) {
        this.width = width;
        this.height = height;

        terrainMap = new Texture(GL_R16F, width, height);
        waterMap = new Texture(GL_R16F, width, height);
        sedimentMap = new Texture(GL_R16F, width, height);
        hardnessMap = new Texture(GL_R16F, width, height);

        waterOutflowPipes = new Texture(GL_RGBA16F, width, height);
        thermalOutflowPipes = new Texture(GL_RGBA32F, width, height);
        velocityField = new Texture(GL_RG16F, width, height);
        sedimentOutflowPipes = new Texture(GL_RGBA16F, width, height);


        addWater = new ComputeProgram("addWater");
        calcWaterOutflow = new ComputeProgram("calcWaterOutflow");
        //applyWaterOutflow = new ComputeProgram("applyWaterOutflow");
        evaporateWater = new ComputeProgram("evaporateWater");


        erosionPrograms = new ComputeProgram[]{
                addWater,
                calcWaterOutflow,
                evaporateWater
        };


        waterMap.bindUniformImage(addWater.program, 1, "waterMap", GL_READ_WRITE);

        terrainMap.bindUniformImage(calcWaterOutflow.program, 0, "terrainMap", GL_READ_ONLY);
        waterMap.bindUniformImage(calcWaterOutflow.program, 1, "waterMap", GL_READ_ONLY);
        waterOutflowPipes.bindUniformImage(calcWaterOutflow.program, 4, "waterOutflowPipes", GL_READ_WRITE);

        waterMap.bindUniformImage(evaporateWater.program, 1, "waterMap", GL_READ_WRITE);

        Main.printErrors();
    }

    public void erosionStep() {
        for (ComputeProgram program : erosionPrograms) {
            glUseProgram(program.program);
            glDispatchCompute(width, height, 1);
            glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        }
    }

    public void printResults() {
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width*height*4*4);

        terrainMap.downloadData(GL_RED, GL_FLOAT, byteBuffer);
        for (int i = 0; i < width*height; i++)
            System.out.print(byteBuffer.getFloat(i*4) + ", ");
        System.out.println();

        waterMap.downloadData(GL_RED, GL_FLOAT, byteBuffer);
        for (int i = 0; i < width*height; i++)
            System.out.print(byteBuffer.getFloat(i*4) + ", ");
        System.out.println();

        waterOutflowPipes.downloadData(GL_RGBA, GL_FLOAT, byteBuffer);
        for (int i = 0; i < width*height*4; i++)
            System.out.print(byteBuffer.getFloat(i*4) + ", ");
        System.out.println();

        System.out.println();
    }

    public void delete() {
        for (ComputeProgram program : erosionPrograms)
            program.delete();
    }
}
