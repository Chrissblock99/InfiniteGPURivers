package me.chriss99;

import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Arrays;

public class VAOGenerator {
    public static double[] heightMapToSimpleVertexes(double[][] heightMap) {
        double[] vertecies = new double[heightMap.length*heightMap[0].length*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                vertecies[vertexShift] = x;
                vertecies[vertexShift + 1] = heightMap[x][z];
                vertecies[vertexShift + 2] = z;

                vertexShift += 3;
            }

        return vertecies;
    }

    public static double[] heightMapToSimpleColors(double[][] heightMap) {
        double[] color = new double[heightMap.length*heightMap[0].length*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                color[vertexShift] = heightMap[x][z]/30*.7+.3;
                color[vertexShift + 1] = heightMap[x][z]/30*.8+.2;
                color[vertexShift + 2] = (x%2==0) ? .33 : 0 + ((z%2==0) ? .33 : 0);

                vertexShift += 3;
            }

        return color;
    }

    public static int[] heightMapToSimpleIndex(double[][] heightMap) {
        int[] index = new int[(heightMap.length-1)*(heightMap[0].length-1)*6];
        int indexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {

                if (z == heightMap[0].length-1 || x == heightMap.length-1)
                    continue;

                index[indexShift+0] = indexOfXZFlattenedArray(x, z, heightMap.length);
                index[indexShift+1] = indexOfXZFlattenedArray(x+1, z, heightMap.length);
                index[indexShift+2] = indexOfXZFlattenedArray(x, z+1, heightMap.length);
                index[indexShift+3] = indexOfXZFlattenedArray(x+1, z, heightMap.length);
                index[indexShift+4] = indexOfXZFlattenedArray(x+1, z+1, heightMap.length);
                index[indexShift+5] = indexOfXZFlattenedArray(x, z+1, heightMap.length);
                indexShift += 6;
            }

        return index;
    }

    public static VAO heightMapToSimpleVAO(double[][] heightMap) {
        double[] vertexes = heightMapToSimpleVertexes(heightMap);
        double[] color = heightMapToSimpleColors(heightMap);
        int[] index = heightMapToSimpleIndex(heightMap);

        return new VAO(vertexes, color, index);
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

    public static VAO heightMapToSquareVAO(double[][] heightMap) {
        double[] vertexes = heightMapToSquareVertexes(heightMap);
        double[] color = heightMapToSquareColors(heightMap);
        int[] index = heightMapToSquareIndex(heightMap);

        return new VAO(vertexes, color, index);
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

    public static VAO heightMapToCrossVAO(double[][] heightMap, double[][][] outflowPipes) {
        double[] vertexes = heightMapToCrossVertexes(heightMap);
        double[] color = heightMapToCrossColors(heightMap, outflowPipes);
        int[] index = heightMapToCrossIndex(heightMap);

        return new VAO(vertexes, color, index);
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

    public static VAO heightMapToVectorVAO(double[][] heightMap, double[][][] vectorField) {
        double[] vertexes = heightMapToVectorVertexes(heightMap, vectorField);
        double[] color = heightMapToVectorColors(heightMap);
        int[] index = heightMapToVectorIndex(heightMap);

        return new VAO(vertexes, color, index);
    }

    public static double[] heightMapToNormalVertexes(double[][] heightMap) {
        double[] vertexes = new double[heightMap.length*heightMap[0].length*3*3];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                Vector3d normal = Main.heightMapTransformer.normalAt(heightMap, x, z);

                vertexes[vertexShift    ] = x - normal.z * .3;
                vertexes[vertexShift + 1] = heightMap[x][z];
                vertexes[vertexShift + 2] = z + normal.x * .3;
                vertexShift += 3;

                vertexes[vertexShift    ] = x + normal.z * .3;
                vertexes[vertexShift + 1] = heightMap[x][z];
                vertexes[vertexShift + 2] = z - normal.x * .3;
                vertexShift += 3;

                vertexes[vertexShift    ] = x + normal.x;
                vertexes[vertexShift + 1] = heightMap[x][z] + normal.y;
                vertexes[vertexShift + 2] = z + normal.z;
                vertexShift += 3;
            }

        return vertexes;
    }

    public static VAO heightMapToNormalVAO(double[][] heightMap) {
        double[] vertexes = heightMapToNormalVertexes(heightMap);
        double[] color = heightMapToVectorColors(heightMap);
        int[] index = heightMapToVectorIndex(heightMap);

        return new VAO(vertexes, color, index);
    }

    private static int indexOfXZFlattenedArray(int x, int z, int xSize) {
        return x + z*xSize;
    }
}
