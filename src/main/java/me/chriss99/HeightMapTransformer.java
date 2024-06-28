package me.chriss99;

import java.util.Arrays;

public class HeightMapTransformer {
    double deltaT = 10; //[0.1;100]
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

    public void simpleThermalErosion(double[][] heightMap) {
        double[][][] thermalOutflowPipes = calculateThermalOutflow(heightMap);
        applyThermalOutflow(heightMap, thermalOutflowPipes);
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

                double heightChange = cellArea * deltaT * thermalErosionRate * terrainHardness(x, z) * maxHeightDiff*.5;
                double inverseSteepNeighbourHeightDiffSum = 1 / steepNeighbourHeightDiffSum;

                //inverseSteepNeighbourHeightDiffSum CAN be Infinite, but in that case all differences are 0 and no one ever calculates anything
                for (int i = 0; i < mooreNeighbourhood.length; i++) {
                    if (!neighborBelowTalusAngle[i])
                        continue;

                    outflowPipes[x][z][i] = heightChange * (heightMap[x][z]-heightMap[wrapOffsetCoordinateMoore(x, heightMap.length, i, 0)][wrapOffsetCoordinateMoore(z, heightMap[0].length, i, 1)]) * inverseSteepNeighbourHeightDiffSum;
                }

                if (!Arrays.equals(neighborBelowTalusAngle, new boolean[8]))
                    heightMap[x][z] -= heightChange;
            }

        return outflowPipes;
    }

    private void applyThermalOutflow(double[][] heightMap, double[][][] outflowPipes) {
        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++)
                for (int i = 0; i < mooreNeighbourhood.length; i++)
                    heightMap[x][z] += outflowPipes[wrapOffsetCoordinateMoore(x, heightMap.length, i, 0)][wrapOffsetCoordinateMoore(z, heightMap[0].length, i, 1)][7-i];
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
