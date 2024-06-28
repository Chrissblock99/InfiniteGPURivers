package me.chriss99;

public class TerrainData {
    public final int xSize;
    public final int zSize;

    public final double[][] terrainMap;
    public final double[][] waterMap;
    public final double[][] sedimentMap;
    //public final double[][] hardnessMap;

    public final double[][][] waterOutFlowPipes;

    public TerrainData(double[][] terrainMap) {
        this.xSize = terrainMap.length;
        this.zSize = terrainMap[0].length;

        this.terrainMap = terrainMap;
        waterMap = new double[xSize][zSize];
        sedimentMap = new double[xSize][zSize];
        //hardnessMap = new double[xSize][zSize];

        waterOutFlowPipes = new double[xSize][zSize][4];
    }

    public double heightAt(int x, int z) {
        return terrainMap[x][z] + waterMap[x][z] + sedimentMap[x][z];
    }

    public double heightDiffTo(int x, int z, int xOffset, int zOffset) {
        return heightAt(x, z) - heightAt((x + xOffset + xSize) % xSize, (z + zOffset + zSize) % zSize);
    }

    public double heightDiffTo(int x, int z, int[] offset) {
        return heightAt(x, z) - heightAt((x + offset[0] + xSize) % xSize, (z + offset[1] + zSize) % zSize);
    }

    public double[][] addedHeights() {
        double[][] allHeights = new double[xSize][zSize];

        for (int z = 0; z < zSize; z++)
            for (int x = 0; x < xSize; x++)
                allHeights[x][z] += heightAt(x, z);

        return allHeights;
    }
}
