package me.chriss99;

import me.chriss99.util.FloatArrayList;
import me.chriss99.worldmanagement.iteration.IterableWorld;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class IterationVAOGenerator {
    public static IterationVAO heightMapToIterationVAO(Vector2i srcPosInChunks, Vector2i sizeInChunks, IterableWorld iterationInfo) {
        FloatArrayList vertecies = new FloatArrayList();

        for (int z = 0; z < sizeInChunks.y; z++)
            for (int x = 0; x < sizeInChunks.x; x++) {
                Vector2i position = new Vector2i(x, z).add(srcPosInChunks);
                IterationSurfaceType surfaceType = iterationInfo.getIterationSurfaceType(position);
                int iteration = iterationInfo.getTile(position).iteration/ iterationInfo.chunkSize;

                Vector3i pos = new Vector3i(position.x, iteration, position.y).mul(iterationInfo.chunkSize);
                addSurface(vertecies, surfaceType, pos, iterationInfo.chunkSize);
            }

        return new IterationVAO(vertecies.getArray(), srcPosInChunks, sizeInChunks.x);
    }

    private static void addSurface(FloatArrayList vertecies, IterationSurfaceType surfaceType, Vector3i pos, int scale) {
        byte bits = surfaceType.toBits();
        byte type = (byte) (bits & 0b1100);
        int dir = (byte) (bits & 0b0011);
        boolean otherOrdering = (type == 0b1000 || type == 0b1100) && (dir == 0b0000 || dir == 0b0011);
        int[][] elevation = surfaceType.getSurface();

        vertecies.add(pos.x);
        vertecies.add(pos.y + elevation[0][0] * scale);
        vertecies.add(pos.z);

        vertecies.add(pos.x + scale);
        vertecies.add(pos.y + elevation[0][1] * scale);
        vertecies.add(pos.z);

        if (!otherOrdering) {
            vertecies.add(pos.x);
            vertecies.add(pos.y + elevation[1][0] * scale);
        } else {
            vertecies.add(pos.x + scale);
            vertecies.add(pos.y + elevation[1][1] * scale);
        }
        vertecies.add(pos.z + scale);

        if (!otherOrdering) {
            vertecies.add(pos.x + scale);
            vertecies.add(pos.y + elevation[0][1] * scale);
            vertecies.add(pos.z);
        }

        vertecies.add(pos.x + scale);
        vertecies.add(pos.y + elevation[1][1] * scale);
        vertecies.add(pos.z + scale);

        vertecies.add(pos.x);
        vertecies.add(pos.y + elevation[1][0] * scale);
        vertecies.add(pos.z + scale);

        if (otherOrdering) {
            vertecies.add(pos.x);
            vertecies.add(pos.y + elevation[0][0] * scale);
            vertecies.add(pos.z);
        }
    }
}
