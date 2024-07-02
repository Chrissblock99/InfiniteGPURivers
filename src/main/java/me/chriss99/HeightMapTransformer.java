package me.chriss99;

import java.util.Arrays;

public class HeightMapTransformer {
    double deltaTThermal = 10; //[0.1;100]
    double deltaTWater = 0.02; //[0;0.05]

    double rainRate = 0.012; //[0;0.05]
    double evaporationRate = 0.015; //[0;0.05]
    double pipeCrossArea = 20; //[0.1;60]
    double gravity = 9.81; //[0.1;20]
    double sedimentCapacity = 1; //[0.1;3]
    double thermalErosionRate = 0.015; //[0;3]
    double soilSuspensionRate = 0.5; //[0.1;2]
    double sedimentDepositionRate = 1; //[0.1;3]
    double sedimentSofteningRate = 5; //[0;10]
    double maxErosionDepth = 10; //[0;40]
    double talusAngleTangentCoeff = 0.8; //[0;1]
    double talusAngleTangentBias = 0.1; //[0;1]

    double cellArea = 1;
    double pipeLength = 1; //only used for water
    double inversePipeLength = 1/pipeLength;

    public void simpleHydraulicErosion(TerrainData terrainData) {
        addWater(terrainData);
        calculateWaterOutflow(terrainData);
        calculateVelocityField(terrainData);
        applyWaterOutflow(terrainData);
        evaporateWater(terrainData);
    }

    public void simpleThermalErosion(double[][] heightMap) {
        double[][][] thermalOutflowPipes = calculateThermalOutflow(heightMap);
        applyThermalOutflow(heightMap, thermalOutflowPipes);
    }

    private void addWater(TerrainData terrainData) {
        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++)
                terrainData.waterMap[x][z] += deltaTWater * rainRate;
    }

    private void calculateWaterOutflow(TerrainData terrainData) {
        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++) {
                double totalOutFlow = 0;

                for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
                    double outFlow = Math.max(0, terrainData.waterOutFlowPipes[x][z][i] +
                            //the paper didn't mention to consider sediment height as well but im doing it anyway
                            deltaTWater * terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]));
                    terrainData.waterOutFlowPipes[x][z][i] = outFlow;
                    totalOutFlow += outFlow;
                }

                if (totalOutFlow > terrainData.waterMap[x][z]) {
                    //System.out.println(terrainData.waterMap[x][z] + " " + totalOutFlow);
                    double flowScalar = terrainData.waterMap[x][z] / totalOutFlow;// * deltaTWater;
                    for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                        terrainData.waterOutFlowPipes[x][z][i] *= flowScalar;

                    //double testTotalOutFlow = 0;
                    //for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                    //    testTotalOutFlow += terrainData.waterOutFlowPipes[x][z][i];

                    //totalOutFlow *= flowScalar;
                    //System.out.println(flowScalar);
                    //System.out.println(totalOutFlow + " " + testTotalOutFlow);
                    //System.out.println();
                }

                /*double totalOutflow = 0;

                for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
                    double outflow = (terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]) > 0) ?
                        Math.min(terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]), terrainData.heightAt(x, z)) * deltaTWater : 0;

                    terrainData.waterOutFlowPipes[x][z][i] = outflow;
                    totalOutflow += outflow;
                }

                if (totalOutflow > terrainData.waterMap[x][z]) {
                    //System.out.println(terrainData.waterMap[x][z] + " " + totalOutflow);
                    double flowScalar = terrainData.waterMap[x][z] * pipeLength * pipeLength / totalOutflow;
                    for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                        terrainData.waterOutFlowPipes[x][z][i] *= flowScalar;

                    //double testTotalOutFlow = 0;
                    //for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                    //    testTotalOutFlow += terrainData.waterOutFlowPipes[x][z][i];

                    //totalOutflow *= flowScalar;
                    //System.out.println(flowScalar);
                    //System.out.println(totalOutflow + " " + testTotalOutFlow);
                    //System.out.println();
                }*/

                /*for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                    if (terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]) > 0)
                        terrainData.waterOutFlowPipes[x][z][i] = terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]) * deltaTWater;
                    else terrainData.waterOutFlowPipes[x][z][i] = 0;*/
            }
    }

    private void applyWaterOutflow(TerrainData terrainData) {
        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++)
                for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
                    terrainData.waterMap[x][z] += terrainData.waterOutFlowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, i, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, i, 1)][3-i];
                    terrainData.waterMap[x][z] -= terrainData.waterOutFlowPipes[x][z][i];
                    if (terrainData.waterOutFlowPipes[x][z][i] < 0)
                        throw new IllegalStateException("WaterOutflow is negative!");
                }
    }

    private void calculateVelocityField(TerrainData terrainData) {
        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++) {
                terrainData.velocityField[x][z][0] += terrainData.waterOutFlowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 1, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 1, 1)][2];
                terrainData.velocityField[x][z][0] -= terrainData.waterOutFlowPipes[x][z][1];
                terrainData.velocityField[x][z][0] -= terrainData.waterOutFlowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 2, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 2, 1)][1];
                terrainData.velocityField[x][z][0] += terrainData.waterOutFlowPipes[x][z][2];

                terrainData.velocityField[x][z][1] -= terrainData.waterOutFlowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 0, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 0, 1)][3];
                terrainData.velocityField[x][z][1] += terrainData.waterOutFlowPipes[x][z][0];
                terrainData.velocityField[x][z][1] += terrainData.waterOutFlowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 3, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 3, 1)][0];
                terrainData.velocityField[x][z][1] -= terrainData.waterOutFlowPipes[x][z][3];
            }
    }

    private void evaporateWater(TerrainData terrainData) {
        double evaporationMultiplier = (1 - evaporationRate * deltaTWater);
        if (evaporationMultiplier < 0)
            //happens when deltaT or evaporationRate are too high
            throw new IllegalStateException("The evaporation multiplier is negative! " + evaporationMultiplier + " Consider lowering deltaT or evaporationRate.");

        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++)
                terrainData.waterMap[x][z] *= evaporationMultiplier;
    }

    private double[][][] calculateThermalOutflow(double[][] heightMap) {
        double[][][] outflowPipes = new double[heightMap.length][heightMap[0].length][8];

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                double maxHeightDiff = -Double.MAX_VALUE;
                boolean[] neighborBelowTalusAngle = new boolean[8];
                double steepNeighbourHeightDiffSum = 0;

                for (int i = 0; i < mooreNeighbourhood.length; i++) {
                    double heightDiff = heightMap[x][z]-heightMap[wrapOffsetCoordinateMoore(x, heightMap.length, i, 0)][wrapOffsetCoordinateMoore(z, heightMap[0].length, i, 1)];
                    if (heightDiff>maxHeightDiff)
                        maxHeightDiff = heightDiff;

                    double angle = heightDiff * inverseMooreNeighbourhoodDistances[i]; //this is normally in atan() but is only used for tan()
                    if (heightDiff > 0 && angle > terrainHardness(x, z) * talusAngleTangentCoeff + talusAngleTangentBias) {
                        neighborBelowTalusAngle[i] = true;
                        steepNeighbourHeightDiffSum += heightDiff;
                    }
                }

                double heightChange = cellArea * deltaTThermal * thermalErosionRate * terrainHardness(x, z) * maxHeightDiff*.5;
                double inverseSteepNeighbourHeightDiffSum = 1 / steepNeighbourHeightDiffSum;

                //inverseSteepNeighbourHeightDiffSum CAN be Infinite, but in that case all differences are 0 and no one ever calculates anything
                for (int i = 0; i < mooreNeighbourhood.length; i++) {
                    if (!neighborBelowTalusAngle[i])
                        continue;

                    outflowPipes[x][z][i] = heightChange * (heightMap[x][z]-heightMap[wrapOffsetCoordinateMoore(x, heightMap.length, i, 0)][wrapOffsetCoordinateMoore(z, heightMap[0].length, i, 1)]) * inverseSteepNeighbourHeightDiffSum;
                }
            }

        return outflowPipes;
    }

    private void applyThermalOutflow(double[][] heightMap, double[][][] outflowPipes) {
        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++)
                for (int i = 0; i < mooreNeighbourhood.length; i++) {
                    heightMap[x][z] += outflowPipes[wrapOffsetCoordinateMoore(x, heightMap.length, i, 0)][wrapOffsetCoordinateMoore(z, heightMap[0].length, i, 1)][7-i];
                    heightMap[x][z] -= outflowPipes[x][z][i];
                }
    }

    //TODO
    private double terrainHardness(int x, int z) {
        return 1;
    }

    private static int wrapOffsetCoordinateMoore(int index, int length, int offset, int xz) {
        return (index + mooreNeighbourhood[offset][xz] + length) % length;
    }

    private static int wrapOffsetCoordinateVonNeumann(int index, int length, int offset, int xz) {
        return (index + vonNeumannNeighbourhood[offset][xz] + length) % length;
    }


    //  0
    //1   2
    //  3
    private static final int[][] vonNeumannNeighbourhood = new int[][]{
            { 0,  1},
            {-1,  0},
            { 1,  0},
            { 0, -1}
    };


    //0 1 2
    //3   4
    //5 6 7
    private static final int[][] mooreNeighbourhood = new int[][]{
            {-1,  1},
            { 0,  1},
            { 1,  1},
            {-1,  0},
            { 1,  0},
            {-1, -1},
            { 0, -1},
            { 1, -1}
    };

    //pi = 3.06146745892 because of this (the simulation doesn't create perfect circles)
    private static final double[] inverseMooreNeighbourhoodDistances = new double[]{
            1/Math.sqrt(2),
            1,
            1/Math.sqrt(2),
            1,
            1,
            1/Math.sqrt(2),
            1,
            1/Math.sqrt(2),
    };
}
