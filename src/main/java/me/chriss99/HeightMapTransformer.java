package me.chriss99;

import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.LinkedList;

public class HeightMapTransformer {
    double deltaTThermal = 1; //[0.1;100]
    double deltaTWater = 0.02; //[0;0.05]

    double rainRate = 0.012; //[0;0.05]
    double evaporationRate = 0.015; //[0;0.05]
    double pipeCrossArea = 20; //[0.1;60]
    double gravity = 9.81; //[0.1;20]
    double sedimentCapacityMultiplier = 1; //[0.1;3]
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

    public void hydraulicErosion(TerrainData terrainData) {
        addWater(terrainData);
        multiThreadProcessor(terrainData, this::calculateWaterOutflow, 10);
        multiThreadProcessor(terrainData, this::calculateVelocityField, 10);
        multiThreadProcessor(terrainData, this::applyWaterOutflow, 10);
        terrainData.addedHeightsCalculated = false;
        multiThreadProcessor(terrainData, this::erosionAndDeposition, 10);
        terrainData.addedHeightsCalculated = false;
        //double[][][] sedimentOutflow = calculateSedimentOutflow(terrainData);
        //applySedimentOutflow(terrainData, sedimentOutflow);
        multiThreadProcessor(terrainData, this::sedimentTransportation, 10);
        terrainData.sedimentMap = terrainData.newSedimentMap;
        evaporateWater(terrainData);
    }

    public void thermalErosion(TerrainData terrainData) {
        multiThreadProcessor(terrainData, this::calculateThermalOutflow, 10);
        multiThreadProcessor(terrainData, this::applyThermalOutflow, 10);
        terrainData.addedHeightsCalculated = false;
    }

    boolean rain = false;
    private void addWater(TerrainData terrainData) {
        //if (rain && Math.random()<.4)
        //    terrainData.waterMap[(int) (Math.random() * terrainData.xSize)][(int) (Math.random() * terrainData.zSize)] += 2;

        //terrainData.waterMap[25][80] += 2;

        if (rain)
        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++)
                terrainData.waterMap[x][z] += deltaTWater * rainRate;
        terrainData.addedHeightsCalculated = false;
    }

    private void calculateWaterOutflow(TerrainData terrainData, int x, int z) {
        double totalOutFlow = 0;

        for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
            double outFlow = Math.max(0, terrainData.waterOutflowPipes[x][z][i] +
                    //the paper didn't mention to consider sediment height as well but im doing it anyway
                    deltaTWater * terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]));
            terrainData.waterOutflowPipes[x][z][i] = outFlow;
            totalOutFlow += outFlow;
        }

        if (totalOutFlow > terrainData.waterMap[x][z]) {
            double flowScalar = terrainData.waterMap[x][z] / totalOutFlow;// * deltaTWater;
            for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                terrainData.waterOutflowPipes[x][z][i] *= flowScalar;
        }

        //generates the same velocityField when only simulating water flow in the long run, is a lot cleaner while doing so, but takes A LOT longer
        /*double totalOutflow = 0;

        for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
            double outflow = (terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]) > 0) ?
                Math.min(terrainData.heightDiffTo(x, z, vonNeumannNeighbourhood[i]), terrainData.heightAt(x, z)) * deltaTWater : 0;

            terrainData.waterOutFlowPipes[x][z][i] = outflow;
            totalOutflow += outflow;
        }

        if (totalOutflow > terrainData.waterMap[x][z]) {
            double flowScalar = terrainData.waterMap[x][z] * pipeLength * pipeLength / totalOutflow;
            for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                terrainData.waterOutFlowPipes[x][z][i] *= flowScalar;
        }*/
    }

    private void applyWaterOutflow(TerrainData terrainData, int x, int z) {
        for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
            terrainData.waterMap[x][z] += terrainData.waterOutflowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, i, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, i, 1)][3-i];
            terrainData.waterMap[x][z] -= terrainData.waterOutflowPipes[x][z][i];
            //this needs to be done due to possible floating point imprecision
            terrainData.waterMap[x][z] = Math.max(0, terrainData.waterMap[x][z]);
            if (terrainData.waterOutflowPipes[x][z][i] < 0)
                throw new IllegalStateException("WaterOutflow is negative!");
        }
    }

    private void calculateVelocityField(TerrainData terrainData, int x, int z) {
        terrainData.velocityField[x][z][0]  = terrainData.waterOutflowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 1, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 1, 1)][2];
        terrainData.velocityField[x][z][0] -= terrainData.waterOutflowPipes[x][z][1];
        terrainData.velocityField[x][z][0] -= terrainData.waterOutflowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 2, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 2, 1)][1];
        terrainData.velocityField[x][z][0] += terrainData.waterOutflowPipes[x][z][2];

        terrainData.velocityField[x][z][1]  = terrainData.waterOutflowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 3, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 3, 1)][0];
        terrainData.velocityField[x][z][1] -= terrainData.waterOutflowPipes[x][z][3];
        terrainData.velocityField[x][z][1] -= terrainData.waterOutflowPipes[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, 0, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, 0, 1)][3];
        terrainData.velocityField[x][z][1] += terrainData.waterOutflowPipes[x][z][0];

        //eliminates spikes when using push behaviour for sediment transportation
        //terrainData.velocityField[x][z][0] = terrainData.waterOutFlowPipes[x][z][2];
        //terrainData.velocityField[x][z][0] -= terrainData.waterOutFlowPipes[x][z][1];

        //terrainData.velocityField[x][z][1] = terrainData.waterOutFlowPipes[x][z][0];
        //terrainData.velocityField[x][z][1] -= terrainData.waterOutFlowPipes[x][z][3];
    }

    private void erosionAndDeposition(TerrainData terrainData, int x, int z) {
        double sedimentCapacity = erosionDepthMultiplier(terrainData.waterMap[x][z]) * sedimentCapacityMultiplier * new Vector2d(terrainData.velocityField[x][z]).length();
        double unusedCapacity = sedimentCapacity - terrainData.sedimentMap[x][z];

        double change = (unusedCapacity > 0) ?
                deltaTWater * terrainHardness(x, z) * soilSuspensionRate * unusedCapacity :
                deltaTWater * sedimentDepositionRate * unusedCapacity;
        terrainData.terrainMap[x][z] -= change;
        terrainData.sedimentMap[x][z] = Math.max(0, terrainData.sedimentMap[x][z] + change);
        terrainData.waterMap[x][z] = Math.max(0, terrainData.waterMap[x][z] + change);
    }

    private void sedimentTransportation(TerrainData terrainData, int x, int z) {
        int[][] closestCells = new int[4][2];
        //[2][3]
        //[0][1]
        double[] pullFrom = new double[]{x - terrainData.velocityField[x][z][0], z - terrainData.velocityField[x][z][1]};

        closestCells[0] = new int[]{(int) Math.floor(pullFrom[0]), (int) Math.floor(pullFrom[1])};
        closestCells[1] = new int[]{(int) Math.ceil (pullFrom[0]), (int) Math.floor(pullFrom[1])};
        closestCells[2] = new int[]{(int) Math.floor(pullFrom[0]), (int) Math.ceil (pullFrom[1])};
        closestCells[3] = new int[]{(int) Math.ceil (pullFrom[0]), (int) Math.ceil (pullFrom[1])};

        for (int i = 0; i < 4; i++)
            closestCells[i] = new int[]{wrapNumber(closestCells[i][0], terrainData.xSize), wrapNumber(closestCells[i][1], terrainData.zSize)};


        double[] interpolation = new double[]{pullFrom[0] % 1, pullFrom[1] % 1};
        double[] heights = new double[4];
        for (int i = 0; i < 4; i++)
            heights[i] = terrainData.sedimentMap[closestCells[i][0]][closestCells[i][1]];

        double down = lerp(heights[0], heights[1], interpolation[0]);
        double up   = lerp(heights[2], heights[3], interpolation[0]);
        terrainData.newSedimentMap[x][z] = lerp(down, up, interpolation[1]);
    }

    private double[][][] calculateSedimentOutflow(TerrainData terrainData) {
        double[][][] sedimentOutflow = new double[terrainData.xSize][terrainData.zSize][4];

        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++) {
                double totalOutFlow = 0;

                double velX = terrainData.velocityField[x][z][0];
                double outFlowX = Math.abs(velX);
                if (velX != 0) {
                    sedimentOutflow[x][z][(velX > 0) ? 2 : 1] = outFlowX;
                    totalOutFlow += outFlowX;
                }

                double velY = terrainData.velocityField[x][z][1];
                double outFlowZ = Math.abs(velY);
                if (velY != 0) {
                    sedimentOutflow[x][z][(velY > 0) ? 0 : 3] = outFlowZ;
                    totalOutFlow += outFlowZ;
                }

                if (totalOutFlow > terrainData.sedimentMap[x][z]) {
                    double flowScalar = terrainData.sedimentMap[x][z] / totalOutFlow;
                    for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
                        sedimentOutflow[x][z][i] *= flowScalar;
                }
            }

        return sedimentOutflow;
    }

    private void applySedimentOutflow(TerrainData terrainData, double[][][] sedimentOutflow) {
        for (int z = 0; z < terrainData.zSize; z++)
            for (int x = 0; x < terrainData.xSize; x++)
                for (int i = 0; i < vonNeumannNeighbourhood.length; i++) {
                    terrainData.sedimentMap[x][z] += sedimentOutflow[wrapOffsetCoordinateVonNeumann(x, terrainData.xSize, i, 0)][wrapOffsetCoordinateVonNeumann(z, terrainData.zSize, i, 1)][3-i];
                    terrainData.sedimentMap[x][z] -= sedimentOutflow[x][z][i];
                    if (sedimentOutflow[x][z][i] < 0)
                        throw new IllegalStateException("SedimentOutflow is negative!");
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
        terrainData.addedHeightsCalculated = false;
    }

    private void calculateThermalOutflow(TerrainData terrainData, int x, int z) {
        double maxHeightDiff = -Double.MAX_VALUE;
        boolean[] neighborBelowTalusAngle = new boolean[8];
        double steepNeighbourHeightDiffSum = 0;

        for (int i = 0; i < mooreNeighbourhood.length; i++) {
            double heightDiff = terrainData.terrainMap[x][z]-terrainData.terrainMap[wrapOffsetCoordinateMoore(x, terrainData.xSize, i, 0)][wrapOffsetCoordinateMoore(z, terrainData.zSize, i, 1)];
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
        for (int i = 0; i < mooreNeighbourhood.length; i++)
            terrainData.thermalOutflowPipes[x][z][i] = (neighborBelowTalusAngle[i]) ?
                    heightChange * (terrainData.terrainMap[x][z]-terrainData.terrainMap[wrapOffsetCoordinateMoore(x, terrainData.xSize, i, 0)][wrapOffsetCoordinateMoore(z, terrainData.zSize, i, 1)]) * inverseSteepNeighbourHeightDiffSum : 0;
    }

    private void applyThermalOutflow(TerrainData terrainData, int x, int z) {
        for (int i = 0; i < mooreNeighbourhood.length; i++) {
            terrainData.terrainMap[x][z] += terrainData.thermalOutflowPipes[wrapOffsetCoordinateMoore(x, terrainData.xSize, i, 0)][wrapOffsetCoordinateMoore(z, terrainData.zSize, i, 1)][7-i];
            terrainData.terrainMap[x][z] -= terrainData.thermalOutflowPipes[x][z][i];
        }
    }

    //TODO
    private double terrainHardness(int x, int z) {
        return 1;
    }

    double inverseMaxErosionDepth = 1/maxErosionDepth;
    private double erosionDepthMultiplier(double depth) {
        if (depth >= maxErosionDepth) return 0;
        if (depth <= 0) return 1;
        return 1 - inverseMaxErosionDepth*depth;
    }

    public Vector3d normalAt(double[][] heightMap, int x, int z) {
        double[] heights = new double[4];
        for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
            heights[i] = heightMap[wrapOffsetCoordinateVonNeumann(x, heightMap.length, i, 0)][wrapOffsetCoordinateVonNeumann(z, heightMap[0].length, i, 1)];

        return new Vector3d(heights[1] - heights[2], 1, heights[3] - heights[0]).normalize();
    }

    public Vector3d flow3dAt(TerrainData terrainData, int x, int z) {
        Vector3d normal = normalAt(terrainData.addedHeights(), x, z);
        Vector3d flowDir = new Vector3d(terrainData.velocityField[x][z][0], 0, terrainData.velocityField[x][z][1]);

        return flowDir.cross(normal).cross(normal).mul(-1);
    }

    private static int wrapOffsetCoordinateMoore(int index, int length, int offset, int xz) {
        return wrapNumber(index + mooreNeighbourhood[offset][xz], length);
    }

    private static int wrapOffsetCoordinateVonNeumann(int index, int length, int offset, int xz) {
        return wrapNumber(index + vonNeumannNeighbourhood[offset][xz], length);
    }

    private static int wrapNumber(int num, int length) {
        return (num + length) % length;
    }

    private static double lerp(double a, double b, double t) {
        return (b - a) * t + a;
    }

    private static void multiThreadProcessor(TerrainData terrainData, TerrainDataProcessor processor, int threadNum) {
        int step = (int) Math.ceil((double) terrainData.zSize / (double) threadNum);

        LinkedList<Thread> threadList = new LinkedList<>();
        for (int z = 0; z < terrainData.zSize; z += step) {
            int zStart = z;
            int zEnd = Math.min(z + step, terrainData.zSize);
            Thread thread = new Thread(() -> {
                for (int smallZ = zStart; smallZ < zEnd; smallZ++)
                    for (int x = 0; x < terrainData.xSize; x++)
                        processor.processAt(terrainData, x, smallZ);
            });
            thread.start();
            threadList.add(thread);
        }

        while (!threadList.isEmpty())
            threadList.removeIf((thread -> !thread.isAlive()));
    }

    private interface TerrainDataProcessor {
        void processAt(TerrainData terrainData, int x, int z);
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
