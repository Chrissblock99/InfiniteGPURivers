package me.chriss99;

public class TerrainVAOGenerator {
    public static float[] heightMapToSimpleVertexes(float[][][] heightMap) {
        float[] vertecies = new float[heightMap[0].length*heightMap[0][0].length*4];
        int vertexShift = 0;

        for (int z = 0; z < heightMap[0][0].length; z++)
            for (int x = 0; x < heightMap[0].length; x++) {
                vertecies[vertexShift] = x;
                vertecies[vertexShift + 1] = z;
                vertecies[vertexShift + 2] = heightMap[0][x][z];

                float waterHeight = heightMap[1][x][z] - .03f;
                vertecies[vertexShift + 3] = heightMap[0][x][z] + waterHeight - ((waterHeight <= 0) ? .1f : 0);

                vertexShift += 4;
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

                index[indexShift+0] = Util.indexOfXZFlattenedArray(x, z, height);
                index[indexShift+1] = Util.indexOfXZFlattenedArray(x+1, z+1, height);
                index[indexShift+2] = Util.indexOfXZFlattenedArray(x, z+1, height);
                index[indexShift+3] = Util.indexOfXZFlattenedArray(x, z, height);
                index[indexShift+4] = Util.indexOfXZFlattenedArray(x+1, z, height);
                index[indexShift+5] = Util.indexOfXZFlattenedArray(x+1, z+1, height);
                indexShift += 6;
            }

        return index;
    }

    public static TerrainVAO heightMapToSimpleVAO(float[][][] heightMap) {
        float[] vertexes = heightMapToSimpleVertexes(heightMap);
        int[] index = heightMapToSimpleIndex(heightMap[0].length, heightMap[0][0].length);

        return new TerrainVAO(vertexes, index);
    }
}
