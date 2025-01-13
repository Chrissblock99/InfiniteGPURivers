package me.chriss99;

import me.chriss99.util.FloatArrayList;
import me.chriss99.worldmanagement.quadtree.IterationSurface;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class IterationVAOGenerator {
    public static IterationVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, ErosionDataStorage data) {
        FloatArrayList vertecies = new FloatArrayList();

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                Vector2i position = new Vector2i(x, z).add(srcPosInChunks);
                IterationSurface surface = data.iterationInfo.getChunk(position.x, position.y);
                IterationSurfaceType surfaceType = new IterationSurfaceType((byte) 0b1101); //TODO; hahahahahaahahahahaha (actually do the tree some time later)

                Vector3i pos = new Vector3i(position.x, surface.getIteration(), position.y);
                addSurface(vertecies, pos, data.chunkSize, surfaceType);
            }

        return new IterationVAO(vertecies.getArray(), srcPosInChunks, sizeInChunks.x);
    }

    private static void addSurface(FloatArrayList vertecies, Vector3i pos, int chunkSize, IterationSurfaceType surfaceType) {
        byte bits = surfaceType.toBits();
        byte type = (byte) (bits & 0b1100);
        int dir = (byte) (bits & 0b0011);
        boolean otherOrdering = (type == 0b1000 || type == 0b1100) && (dir == 0b0000 || dir == 0b0011);
        int[][] elevation = surfaceType.getSurface();

        vertecies.add(pos.x * chunkSize);
        vertecies.add(pos.y + elevation[0][0] * chunkSize);
        vertecies.add(pos.z * chunkSize);

        vertecies.add((pos.x + 1) * chunkSize);
        vertecies.add(pos.y + elevation[0][1] * chunkSize);
        vertecies.add(pos.z * chunkSize);

        if (!otherOrdering) {
            vertecies.add(pos.x * chunkSize);
            vertecies.add(pos.y + elevation[1][0] * chunkSize);
        } else {
            vertecies.add((pos.x + 1) * chunkSize);
            vertecies.add(pos.y + elevation[1][1] * chunkSize);
        }
        vertecies.add((pos.z + 1) * chunkSize);

        if (!otherOrdering) {
            vertecies.add((pos.x + 1) * chunkSize);
            vertecies.add(pos.y + elevation[0][1] * chunkSize);
            vertecies.add(pos.z * chunkSize);
        }

        vertecies.add((pos.x + 1) * chunkSize);
        vertecies.add(pos.y + elevation[1][1] * chunkSize);
        vertecies.add((pos.z + 1) * chunkSize);

        vertecies.add(pos.x * chunkSize);
        vertecies.add(pos.y + elevation[1][0] * chunkSize);
        vertecies.add((pos.z + 1) * chunkSize);

        if (otherOrdering) {
            vertecies.add(pos.x * chunkSize);
            vertecies.add(pos.y + elevation[0][0] * chunkSize);
            vertecies.add(pos.z * chunkSize);
        }
    }
}
