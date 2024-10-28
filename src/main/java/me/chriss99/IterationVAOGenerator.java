package me.chriss99;

import org.joml.Vector2i;

public class IterationVAOGenerator {
    public static IterationVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        float[] vertecies = new float[sizeInChunks.x* sizeInChunks.y*3*6];
        int vertexShift = 0;

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                Vector2i position = new Vector2i(x, z).add(srcPosInChunks);
                IterationSurfaceType surfaceType = data.iterationSurfaceTypeOf(position);

                vertecies[vertexShift+0] = (srcPosInChunks.x + x) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[0][0]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z) * data.chunkSize;
                vertexShift += 3;

                vertecies[vertexShift+0] = (srcPosInChunks.x + x + 1) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[0][1]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z) * data.chunkSize;
                vertexShift += 3;

                vertecies[vertexShift+0] = (srcPosInChunks.x + x) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[1][0]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z + 1) * data.chunkSize;
                vertexShift += 3;

                vertecies[vertexShift+0] = (srcPosInChunks.x + x + 1) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[0][1]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z) * data.chunkSize;
                vertexShift += 3;

                vertecies[vertexShift+0] = (srcPosInChunks.x + x + 1) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[1][1]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z + 1) * data.chunkSize;
                vertexShift += 3;

                vertecies[vertexShift+0] = (srcPosInChunks.x + x) * data.chunkSize;
                vertecies[vertexShift+1] = data.iterationOf(position) + surfaceType.getSurface()[1][0]* data.chunkSize;
                vertecies[vertexShift+2] = (srcPosInChunks.y + z + 1) * data.chunkSize;
                vertexShift += 3;
            }

        return new IterationVAO(vertecies, srcPosInChunks, sizeInChunks.x);
    }
}
