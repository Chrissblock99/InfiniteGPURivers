package me.chriss99;

public class HeightMapGenerator {
    public static double[][] randomHeights(int xSize, int zSize) {
        double[][] heights = new double[xSize][zSize];

        for (int x = 0; x < xSize; x++)
            for (int z = 0; z < zSize; z++)
                heights[x][z] = Math.random()*3;

        return heights;
    }

    public static double[][] pillar(int xSize, int zSize) {
        double[][] heights = new double[xSize][zSize];

        for (int x = 0; x < xSize; x++)
            for (int z = 0; z < zSize; z++)
                heights[x][z] = (x > xSize*0.4 && x < xSize*0.6 && z > zSize*0.4 && z < zSize*0.6) ? 30 : 0;

        return heights;
    }

    public static double[][] pillars(int xSize, int zSize) {
        double[][] heights = new double[xSize][zSize];

        for (int x = 0; x < xSize; x++)
            for (int z = 0; z < zSize; z++)
                heights[x][z] =
                        (x > xSize*0.6 && x < xSize*0.7 && z > zSize*0.3 && z < zSize*0.4) ||
                        (x > xSize*0.45 && x < xSize*0.55 && z > zSize*0.45 && z < zSize*0.55) ||
                        (x > xSize*0.3 && x < xSize*0.4 && z > zSize*0.6 && z < zSize*0.7) ? 120 : 0;

        return heights;
    }

    public static double[][] spike(int xSize, int zSize) {
        double[][] heights = new double[xSize][zSize];

        heights[50][50] = 30;

        return heights;
    }

    public static double[][] slope(int xSize, int zSize, double slope) {
        double[][] heights = new double[xSize][zSize];

        for (int z = 0; z < zSize; z++)
            for (int x = 0; x < xSize; x++)
                heights[x][z] = z * slope;

        return heights;
    }

    public static double[][] slopeWithBarrier(int xSize, int zSize, double slope) {
        double[][] heights = new double[xSize][zSize];

        for (int z = 0; z < zSize; z++)
            for (int x = 0; x < xSize; x++) {
                heights[x][z] = z * slope;
                if (x <= 50 && z <= 30)
                    heights[x][z] += 10;
            }

        return heights;
    }
}
