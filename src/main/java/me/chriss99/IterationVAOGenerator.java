package me.chriss99;

import org.joml.Vector2i;

public class IterationVAOGenerator {
    public static IterationVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        float[] vertecies = new float[sizeInChunks.x* sizeInChunks.y*3];
        int[] index = new int[(sizeInChunks.x-1)*(sizeInChunks.y-1)*6];

        int vertexShift = 0;
        int indexShift = 0;

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                Vector2i position = new Vector2i(x, z).add(srcPosInChunks);
                IterationSurfaceType surfaceType = data.iterationSurfaceTypeOf(position);

                vertecies[vertexShift+0] = (srcPosInChunks.x + x) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[0][0]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z) * data.chunkSize;
                vertexShift += 3;


                if (z == sizeInChunks.y-1 || x == sizeInChunks.x-1)
                    continue;

                index[indexShift+0] = Util.indexOfXZFlattenedArray(x, z, sizeInChunks.x);
                index[indexShift+1] = Util.indexOfXZFlattenedArray(x+1, z, sizeInChunks.x);
                index[indexShift+2] = Util.indexOfXZFlattenedArray(x, z+1, sizeInChunks.x);
                index[indexShift+3] = Util.indexOfXZFlattenedArray(x+1, z, sizeInChunks.x);
                index[indexShift+4] = Util.indexOfXZFlattenedArray(x+1, z+1, sizeInChunks.x);
                index[indexShift+5] = Util.indexOfXZFlattenedArray(x, z+1, sizeInChunks.x);
                indexShift += 6;
            }

        return new IterationVAO(vertecies, index, srcPosInChunks, sizeInChunks.x);
    }
}
