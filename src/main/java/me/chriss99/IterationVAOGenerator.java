package me.chriss99;

import org.joml.Vector2i;

public class IterationVAOGenerator {
    public static float[] heightMapToIterationVertexes(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        float[] vertecies = new float[sizeInChunks.x* sizeInChunks.y];
        int vertexShift = 0;

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                Vector2i position = new Vector2i(x, z).add(srcPosInChunks);
                vertecies[vertexShift] = data.iterationOf(position) + data.iterationSurfaceTypeOf(position).getSurface()[0][0]* data.chunkSize;

                vertexShift++;
            }

        return vertecies;
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

    public static IterationVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        float[] vertexes = heightMapToIterationVertexes(srcPosInChunks, sizeInChunks, data);
        int[] index = heightMapToSimpleIndex(sizeInChunks);

        return new IterationVAO(vertexes, index, srcPosInChunks, sizeInChunks.x);
    }
}
