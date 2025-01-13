package me.chriss99;

import me.chriss99.util.FloatArrayList;
import me.chriss99.worldmanagement.quadtree.IterationSurface;
import org.joml.Vector2i;

public class IterationVAOGenerator {
    public static IterationVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        FloatArrayList vertecies = new FloatArrayList();

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                Vector2i position = new Vector2i(x, z).add(srcPosInChunks);
                IterationSurface surface = data.iterationInfo.getChunk(position.x, position.y);
                IterationSurfaceType surfaceType = new IterationSurfaceType((byte) 0b0000); //TODO; hahahahahaahahahahaha (actually do the tree some time later)

                byte bits = surfaceType.toBits();
                byte type = (byte) (bits & 0b1100);
                int dir = (byte) (bits & 0b0011);
                boolean otherOrdering = (type == 0b1000 || type == 0b1100) && (dir == 0b0000 || dir == 0b0011);
                int iteration = surface.getIteration();
                int[][] elevation = surfaceType.getSurface();

                vertecies.add((srcPosInChunks.x + x) * data.chunkSize);
                vertecies.add(iteration + elevation[0][0] * data.chunkSize);
                vertecies.add((srcPosInChunks.y + z) * data.chunkSize);

                vertecies.add((srcPosInChunks.x + x + 1) * data.chunkSize);
                vertecies.add(iteration + elevation[0][1] * data.chunkSize);
                vertecies.add((srcPosInChunks.y + z) * data.chunkSize);

                if (!otherOrdering) {
                    vertecies.add((srcPosInChunks.x + x) * data.chunkSize);
                    vertecies.add(iteration + elevation[1][0] * data.chunkSize);
                } else {
                    vertecies.add((srcPosInChunks.x + x + 1) * data.chunkSize);
                    vertecies.add(iteration + elevation[1][1] * data.chunkSize);
                }
                vertecies.add((srcPosInChunks.y + z + 1) * data.chunkSize);

                if (!otherOrdering) {
                    vertecies.add((srcPosInChunks.x + x + 1) * data.chunkSize);
                    vertecies.add(iteration + elevation[0][1] * data.chunkSize);
                    vertecies.add((srcPosInChunks.y + z) * data.chunkSize);
                }

                vertecies.add((srcPosInChunks.x + x + 1) * data.chunkSize);
                vertecies.add(iteration + elevation[1][1] * data.chunkSize);
                vertecies.add((srcPosInChunks.y + z + 1) * data.chunkSize);

                vertecies.add((srcPosInChunks.x + x) * data.chunkSize);
                vertecies.add(iteration + elevation[1][0] * data.chunkSize);
                vertecies.add((srcPosInChunks.y + z + 1) * data.chunkSize);

                if (otherOrdering) {
                    vertecies.add((srcPosInChunks.x + x) * data.chunkSize);
                    vertecies.add(iteration + elevation[0][0] * data.chunkSize);
                    vertecies.add((srcPosInChunks.y + z) * data.chunkSize);
                }
            }

        return new IterationVAO(vertecies.getArray(), srcPosInChunks, sizeInChunks.x);
    }
}
