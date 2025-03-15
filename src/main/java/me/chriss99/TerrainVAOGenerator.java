package me.chriss99;

import me.chriss99.util.Util;
import org.joml.Vector2i;

public class TerrainVAOGenerator {
    public static float[] heightMapToSimpleVertexes(Float2DBufferWrapper terrain, Float2DBufferWrapper water) {
        float[] vertecies = new float[terrain.getSize().x* terrain.getSize().y*2];
        int vertexShift = 0;

        for (int z = 0; z < terrain.getSize().y; z++)
            for (int x = 0; x < terrain.getSize().x; x++) {
                float terrainHeight = terrain.getFloat(x, z);
                vertecies[vertexShift] = terrainHeight;

                float waterHeight = water.getFloat(x, z) - .03f;
                vertecies[vertexShift + 1] = terrainHeight + waterHeight - ((waterHeight <= 0) ? .1f : 0);

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

    public static TerrainVAO heightMapToSimpleVAO(Float2DBufferWrapper terrain, Float2DBufferWrapper water, Vector2i srcPos, int scale) {
        float[] vertices = heightMapToSimpleVertexes(terrain, water);
        int[] index = heightMapToSimpleIndex(terrain.getSize().x, terrain.getSize().y);

        return new TerrainVAO(vertices, index, srcPos, terrain.getSize().x, scale);
    }
}
