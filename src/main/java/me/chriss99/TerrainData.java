package me.chriss99;

import org.joml.Vector3d;

import java.util.Arrays;

public class TerrainData {
    public final int xSize;
    public final int zSize;

    public final double[][] terrainMap;
    public final double[][] waterMap;
    public double[][] sedimentMap;
    public final double[][] addedHeights;
    public boolean addedHeightsCalculated = false;
    public final double[][] hardnessMap;

    public final double[][][] waterOutflowPipes;
    public double[][][] thermalOutflowPipes;
    public final double[][][] velocityField;
    public final double[][][] sedimentOutflowPipes;

    public TerrainData(double[][] terrainMap) {
        this.xSize = terrainMap.length;
        this.zSize = terrainMap[0].length;

        this.terrainMap = terrainMap;
        waterMap = new double[xSize][zSize];
        sedimentMap = new double[xSize][zSize];
        addedHeights = new double[xSize][zSize];
        hardnessMap = new double[xSize][zSize];
        for (int x = 0; x < xSize; x++)
            Arrays.fill(hardnessMap[x], 1);

        waterOutflowPipes = new double[xSize][zSize][4];
        thermalOutflowPipes = new double[xSize][zSize][8];
        velocityField = new double[xSize][zSize][2];
        sedimentOutflowPipes = new double[xSize][zSize][4];
    }

    public double heightAt(int x, int z) {
        return terrainMap[x][z] + waterMap[x][z];
    }

    public double heightDiffTo(int x, int z, int xOffset, int zOffset) {
        return heightAt(x, z) - heightAt((x + xOffset + xSize) % xSize, (z + zOffset + zSize) % zSize);
    }

    public double heightDiffTo(int x, int z, int[] offset) {
        return heightAt(x, z) - heightAt((x + offset[0] + xSize) % xSize, (z + offset[1] + zSize) % zSize);
    }

    public double terrainHeightDiffTo(int x, int z, int[] offset) {
        return terrainMap[x][z] - terrainMap[(x + offset[0] + xSize) % xSize][(z + offset[1] + zSize) % zSize];
    }

    public static Vector3d normalAt(double[][] heightMap, int x, int z) {
        double[] heights = new double[4];
        for (int i = 0; i < HeightMapTransformer.vonNeumannNeighbourhood.length; i++)
            heights[i] = heightMap[HeightMapTransformer.wrapOffsetCoordinateVonNeumann(x, heightMap.length, i, 0)][HeightMapTransformer.wrapOffsetCoordinateVonNeumann(z, heightMap[0].length, i, 1)];

        return new Vector3d(heights[1] - heights[2], 1, heights[3] - heights[0]).normalize();
    }

    public double[][] addedHeights() {
        if (!addedHeightsCalculated) {
            for (int z = 0; z < zSize; z++)
                for (int x = 0; x < xSize; x++)
                    addedHeights[x][z] = heightAt(x, z);
            addedHeightsCalculated = true;
        }

        return addedHeights;
    }
}
