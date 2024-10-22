package me.chriss99;

import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;

import java.util.Arrays;

public class ColoredVAOGenerator {
    public static double[] heightMapToSimpleVertexes(double[][] heightMap, boolean water) {
        double[] vertecies = new double[heightMap.length*heightMap[0].length*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                vertecies[vertexShift] = x;
                vertecies[vertexShift + 1] = heightMap[x][z] - ((water) ? .03 : 0);
                vertecies[vertexShift + 2] = z;

                vertexShift += 3;
            }

        return vertecies;
    }

    public static double[] heightMapToSimpleColors(double[][] heightMap, double min, double max, boolean water) {
        double[] color = new double[heightMap.length*heightMap[0].length*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                double gradient = (heightMap[x][z] - min) / (max - min);

                if (!water) {
                    color[vertexShift  ] = gradient*.7+.3;
                    color[vertexShift+1] = gradient*.8+.2;
                    color[vertexShift+2] = (x%2==0) ? .33 : 0 + ((z%2==0) ? .33 : 0);
                } else {
                    color[vertexShift  ] = 0;
                    color[vertexShift+1] = (x%2==0) ? .33 : 0 + ((z%2==0) ? .33 : 0);
                    color[vertexShift+2] = gradient;
                }

                vertexShift += 3;
            }

        return color;
    }

    public static int[] heightMapToSimpleIndex(Vector2i size) {
        int[] index = new int[(size.x-1)*(size.y-1)*6];
        int indexShift = 0;

        for (int z = 0; z < size.y; z++)
            for (int x = 0; x < size.x; x++) {

                if (z == size.y-1 || x == size.x-1)
                    continue;

                index[indexShift+0] = Util.indexOfXZFlattenedArray(x, z, size.x);
                index[indexShift+1] = Util.indexOfXZFlattenedArray(x+1, z, size.x);
                index[indexShift+2] = Util.indexOfXZFlattenedArray(x, z+1, size.x);
                index[indexShift+3] = Util.indexOfXZFlattenedArray(x+1, z, size.x);
                index[indexShift+4] = Util.indexOfXZFlattenedArray(x+1, z+1, size.x);
                index[indexShift+5] = Util.indexOfXZFlattenedArray(x, z+1, size.x);
                indexShift += 6;
            }

        return index;
    }

    public static ColoredVAO heightMapToSimpleVAO(double[][] heightMap, double min, double max, boolean water) {
        double[] vertexes = heightMapToSimpleVertexes(heightMap, water);
        double[] color = heightMapToSimpleColors(heightMap, min, max, water);
        int[] index = heightMapToSimpleIndex(new Vector2i(heightMap.length, heightMap[0].length));

        return new ColoredVAO(vertexes, color, index);
    }

    public static double[] heightMapToIterationVertexes(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        double[] vertecies = new double[sizeInChunks.x*sizeInChunks.y*3];
        int vertexShift = 0;

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                vertecies[vertexShift] = (x + srcPosInChunks.x) * data.chunkSize;
                //TODO: this needs to account for the surface type
                vertecies[vertexShift + 1] = data.iterationOf(new Vector2i(x, z).add(srcPosInChunks));
                vertecies[vertexShift + 2] = (z + srcPosInChunks.y) * data.chunkSize;

                vertexShift += 3;
            }

        return vertecies;
    }

    public static double[] heightMapToIterationColors(Vector2i sizeInChunks) {
        double[] color = new double[sizeInChunks.x*sizeInChunks.y*3];
        int vertexShift = 0;

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                color[vertexShift  ] = .5*.7+.3;
                color[vertexShift+1] = .5*.8+.2;
                color[vertexShift+2] = (x%2==0) ? .33 : 0 + ((z%2==0) ? .33 : 0);

                vertexShift += 3;
            }

        return color;
    }

    public static ColoredChunkVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        double[] vertexes = heightMapToIterationVertexes(srcPosInChunks, sizeInChunks, data);
        double[] color = heightMapToIterationColors(sizeInChunks);
        int[] index = heightMapToSimpleIndex(sizeInChunks);

        return new ColoredChunkVAO(vertexes, color, index, srcPosInChunks, sizeInChunks.x);
    }

    public static double[] heightMapToSquareVertexes(double[][] heightMap) {
        double[] vertexes = new double[heightMap.length*heightMap[0].length*4*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++)
                for (int n = 0; n < 4; n++) {
                    vertexes[vertexShift] = x + ((n>1) ? 1:0) - .5;
                    vertexes[vertexShift + 1] = heightMap[x][z];
                    vertexes[vertexShift + 2] = z + ((n%2==0) ? 1:0) - .5;
                    vertexShift += 3;
                }

        return vertexes;
    }

    public static double[] heightMapToSquareColors(double[][] heightMap) {
        double[] color = new double[heightMap.length*heightMap[0].length*4*3];
        for (int i = 0; i < color.length; i++)
            color[i] = Math.random()*.5+.5;

        return color;
    }

    public static int[] heightMapToSquareIndex(double[][] heightMap) {
        int[] index = new int[heightMap.length*heightMap[0].length*6];
        int indexShift = 0;
        int indexShift2 = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                index[indexShift+0] = indexShift2+0;
                index[indexShift+1] = indexShift2+1;
                index[indexShift+2] = indexShift2+2;
                index[indexShift+3] = indexShift2+1;
                index[indexShift+4] = indexShift2+3;
                index[indexShift+5] = indexShift2+2;
                indexShift += 6;
                indexShift2 += 4;
            }

        return index;
    }

    public static ColoredVAO heightMapToSquareVAO(double[][] heightMap) {
        double[] vertexes = heightMapToSquareVertexes(heightMap);
        double[] color = heightMapToSquareColors(heightMap);
        int[] index = heightMapToSquareIndex(heightMap);

        return new ColoredVAO(vertexes, color, index);
    }

    private static final double[][] offsets = new double[][]{
            { .5,  .5},
            {-.5,  .5},
            {-.5,  .5},
            {-.5, -.5},
            { .5, -.5},
            { .5,  .5},
            {-.5, -.5},
            { .5, -.5}
    };

    public static double[] heightMapToCrossVertexes(double[][] heightMap) {
        double[] vertexes = new double[heightMap.length*heightMap[0].length*9*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                for (int i = 0; i < 8; i++) {
                    vertexes[vertexShift    ] = x + offsets[i][0];
                    vertexes[vertexShift + 1] = heightMap[x][z];
                    vertexes[vertexShift + 2] = z + offsets[i][1];
                    vertexShift += 3;
                }

                vertexes[vertexShift    ] = x;
                vertexes[vertexShift + 1] = heightMap[x][z];
                vertexes[vertexShift + 2] = z;
                vertexShift += 3;
            }

        return vertexes;
    }

    public static double[] heightMapToCrossColors(double[][] heightMap, double[][][] outflowPipes) {
        double[] color = new double[heightMap.length*heightMap[0].length*9*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                for (int i = 0; i < 8 ; i++) {
                    color[vertexShift    ] = -outflowPipes[x][z][i/2]*100;
                    color[vertexShift + 1] =  outflowPipes[x][z][i/2]*100;
                    color[vertexShift + 2] = 0;
                    vertexShift += 3;
                }

                color[vertexShift    ] =  0;
                color[vertexShift + 1] =  0;
                color[vertexShift + 2] = .5;
                vertexShift += 3;
            }

        return color;
    }

    public static int[] heightMapToCrossIndex(double[][] heightMap) {
        int[] index = new int[heightMap.length*heightMap[0].length*12];
        int indexShift = 0;
        int indexShift2 = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                int indexShift3 = 0;
                for (int i = 0; i < 4; i++) {
                    index[indexShift] = indexShift2 + indexShift3;
                    index[indexShift + 1] = indexShift2 + indexShift3 + 1;
                    index[indexShift + 2] = indexShift2 + 8;
                    indexShift3 += 2;
                    indexShift += 3;
                }
                indexShift2 += 9;
            }

        return index;
    }

    public static ColoredVAO heightMapToCrossVAO(double[][] heightMap, double[][][] outflowPipes) {
        double[] vertexes = heightMapToCrossVertexes(heightMap);
        double[] color = heightMapToCrossColors(heightMap, outflowPipes);
        int[] index = heightMapToCrossIndex(heightMap);

        return new ColoredVAO(vertexes, color, index);
    }

    public static double[] heightMapToVectorVertexes(double[][] heightMap, double[][][] vectorField) {
        double[] vertexes = new double[heightMap.length*heightMap[0].length*3*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                Vector2d vector = new Vector2d(vectorField[x][z][0], vectorField[x][z][1]);
                vector.normalize().mul(.3);

                vertexes[vertexShift    ] = x - vector.y * .6 - vector.x;
                vertexes[vertexShift + 1] = heightMap[x][z] + .1;
                vertexes[vertexShift + 2] = z + vector.x * .6 - vector.y;
                vertexShift += 3;

                vertexes[vertexShift    ] = x + vector.y * .6 - vector.x;
                vertexes[vertexShift + 1] = heightMap[x][z] + .1;
                vertexes[vertexShift + 2] = z - vector.x * .6 - vector.y;
                vertexShift += 3;

                vertexes[vertexShift    ] = x + vector.x * 1.5;
                vertexes[vertexShift + 1] = heightMap[x][z] + .1;
                vertexes[vertexShift + 2] = z + vector.y * 1.5;
                vertexShift += 3;
            }

        return vertexes;
    }

    public static double[] heightMapToVectorColors(double[][] heightMap) {
        double[] color = new double[heightMap.length * heightMap[0].length * 3 * 3];
        Arrays.fill(color, 1);

        return color;
    }

    public static int[] heightMapToVectorIndex(double[][] heightMap) {
        int[] index = new int[heightMap.length*heightMap[0].length*3];
        int indexShift = 0;
        int indexShift2 = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                index[indexShift  ] = indexShift2  ;
                index[indexShift+1] = indexShift2+1;
                index[indexShift+2] = indexShift2+2;
                indexShift += 3;
                indexShift2 += 3;
            }

        return index;
    }

    public static ColoredVAO heightMapToVectorVAO(double[][] heightMap, double[][][] vectorField) {
        double[] vertexes = heightMapToVectorVertexes(heightMap, vectorField);
        double[] color = heightMapToVectorColors(heightMap);
        int[] index = heightMapToVectorIndex(heightMap);

        return new ColoredVAO(vertexes, color, index);
    }

    public static double[] heightMapToNormalVertexes(double[][] heightMap) {
        double[] vertexes = new double[heightMap.length*heightMap[0].length*3*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                Vector3d normal = normalAt(heightMap, x, z);

                vertexes[vertexShift    ] = x - normal.z * .3;
                vertexes[vertexShift + 1] = heightMap[x][z] + .1;
                vertexes[vertexShift + 2] = z + normal.x * .3;
                vertexShift += 3;

                vertexes[vertexShift    ] = x + normal.z * .3;
                vertexes[vertexShift + 1] = heightMap[x][z] + .1;
                vertexes[vertexShift + 2] = z - normal.x * .3;
                vertexShift += 3;

                vertexes[vertexShift    ] = x + normal.x;
                vertexes[vertexShift + 1] = heightMap[x][z] + normal.y + .1;
                vertexes[vertexShift + 2] = z + normal.z;
                vertexShift += 3;
            }

        return vertexes;
    }

    public static ColoredVAO heightMapToNormalVAO(double[][] heightMap) {
        double[] vertexes = heightMapToNormalVertexes(heightMap);
        double[] color = heightMapToVectorColors(heightMap);
        int[] index = heightMapToVectorIndex(heightMap);

        return new ColoredVAO(vertexes, color, index);
    }

    public static double[] tesselationGridVertexesTest(int xSize, int zSize, double step) {
        double[] vertexes = new double[xSize*zSize*8];
        int i = 0;

        for (int z = 0; z < zSize; z++)
            for (int x = 0; x < xSize; x++) {
                vertexes[i] = x*step;
                vertexes[i+1] = z*step;

                vertexes[i+2] = (x+1)*step;
                vertexes[i+3] = z*step;

                vertexes[i+4] = x*step;
                vertexes[i+5] = (z+1)*step;

                vertexes[i+6] = (x+1)*step;
                vertexes[i+7] = (z+1)*step;

                i += 8;
            }

        return vertexes;
    }

    public static Vector3d normalAt(double[][] heightMap, int x, int z) {
        double[] heights = new double[4];
        for (int i = 0; i < vonNeumannNeighbourhood.length; i++)
            heights[i] = heightMap[wrapOffsetCoordinateVonNeumann(x, heightMap.length, i, 0)][wrapOffsetCoordinateVonNeumann(z, heightMap[0].length, i, 1)];

        return new Vector3d(heights[1] - heights[2], 1, heights[3] - heights[0]).normalize();
    }

    public static int wrapOffsetCoordinateVonNeumann(int index, int length, int offset, int xz) {
        return wrapNumber(index + vonNeumannNeighbourhood[offset][xz], length);
    }

    //  0
    //1   2
    //  3
    public static final int[][] vonNeumannNeighbourhood = new int[][]{
            { 0,  1},
            {-1,  0},
            { 1,  0},
            { 0, -1}
    };

    public static int wrapNumber(int num, int length) {
        return (num + length) % length;
    }
}
