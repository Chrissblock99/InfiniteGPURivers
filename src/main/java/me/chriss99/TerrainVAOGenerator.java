package me.chriss99;

import org.joml.Vector2i;

public class TerrainVAOGenerator {
    public static float[] heightMapToSimpleVertexes(float[][][] heightMap) {
        float[] vertecies = new float[heightMap[0].length*heightMap[0][0].length*2];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0][0].length; z++)
            for (int x = 0; x < heightMap[0].length; x++) {
                vertecies[vertexShift] = heightMap[0][x][z];

                float waterHeight = heightMap[1][x][z] - .03f;
                vertecies[vertexShift + 1] = heightMap[0][x][z] + waterHeight - ((waterHeight <= 0) ? .1f : 0);

                vertexShift += 2;
            }

        return vertecies;
    }

    public static int[] heightMapToSimpleIndex(int width, int height) {
        int[] index = new int[(width-1)*(height-1)*6];
        int indexShift = 0;

        for (int z = 0; z < height; z++)
            for (int x = 0; x < width; x++) {

                if (z == height-1 || x == width-1)
                    continue;

                index[indexShift+0] = Util.indexOfXZFlattenedArray(x, z, width);
                index[indexShift+1] = Util.indexOfXZFlattenedArray(x+1, z+1, width);
                index[indexShift+2] = Util.indexOfXZFlattenedArray(x, z+1, width);
                index[indexShift+3] = Util.indexOfXZFlattenedArray(x, z, width);
                index[indexShift+4] = Util.indexOfXZFlattenedArray(x+1, z, width);
                index[indexShift+5] = Util.indexOfXZFlattenedArray(x+1, z+1, width);
                indexShift += 6;
            }

        return index;
    }

    public static TerrainVAO heightMapToSimpleVAO(float[][][] heightMap, Vector2i srcPos) {
        float[] vertexes = heightMapToSimpleVertexes(heightMap);
        int[] index = heightMapToSimpleIndex(heightMap[0].length, heightMap[0][0].length);

        return new TerrainVAO(vertexes, index, srcPos, heightMap[0].length);
    }
}
