package me.chriss99;

public class VAOGenerator {
    public static float[][] randomHeights(int xSize, int zSize) {
        float[][] heights = new float[xSize][zSize];

        for (int x = 0; x < xSize; x++)
            for (int z = 0; z < zSize; z++)
                heights[x][z] = (float) (Math.random()*3);

        return heights;
    }

    public static float[][] pillar(int xSize, int zSize) {
        float[][] heights = new float[xSize][zSize];

        for (int x = 0; x < xSize; x++)
            for (int z = 0; z < zSize; z++)
                heights[x][z] = (x > xSize*0.4 && x < xSize*0.6 && z > zSize*0.4 && z < zSize*0.6) ? 30 : 0;

        return heights;
    }

    public static VAO heightMapToSimpleVAO(float[][] heightMap) {
        double[] vertecies = new double[heightMap.length*heightMap[0].length*3];
        double[] color = new double[heightMap.length*heightMap[0].length*3];
        int[] index = new int[(heightMap.length-1)*(heightMap[0].length-1)*6];

        int vertexShift = 0;
        int indexShift = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                vertecies[vertexShift] = x;
                vertecies[vertexShift + 1] = heightMap[x][z];
                vertecies[vertexShift + 2] = z;

                color[vertexShift] = heightMap[x][z]/30*.7+.3;
                color[vertexShift + 1] = heightMap[x][z]/30*.8+.2;
                color[vertexShift + 2] = heightMap[x][z]/30*.9+.1;

                vertexShift += 3;

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


        return new VAO(vertecies, color, index);
    }

    public static VAO heightMapToCubeVAO(float[][] heightMap) {
        double[] vertecies = new double[heightMap.length*heightMap[0].length*4*3];
        int[] index = new int[heightMap.length*heightMap[0].length*6];

        int vertexShift = 0;
        int indexShift = 0;
        int indexShift2 = 0;

        for (int z = 0; z < heightMap[0].length; z++)
            for (int x = 0; x < heightMap.length; x++) {
                for (int n = 0; n < 4; n++) {
                    vertecies[vertexShift] = x + ((n>1) ? 1:0);
                    vertecies[vertexShift + 1] = heightMap[x][z];
                    vertecies[vertexShift + 2] = z + ((n%2==0) ? 1:0);
                    vertexShift += 3;
                }

                index[indexShift+0] = indexShift2+0;
                index[indexShift+1] = indexShift2+1;
                index[indexShift+2] = indexShift2+2;
                index[indexShift+3] = indexShift2+1;
                index[indexShift+4] = indexShift2+3;
                index[indexShift+5] = indexShift2+2;
                indexShift += 6;
                indexShift2 += 4;
            }

        double[] color = new double[heightMap.length*heightMap[0].length*4*3];
        for (int i = 0; i < color.length; i++)
            color[i] = Math.random();

        return new VAO(vertecies, color, index);
    }

    private static int indexOfXZFlattenedArray(int x, int z, int xSize) {
        return x + z*xSize;
    }
}
