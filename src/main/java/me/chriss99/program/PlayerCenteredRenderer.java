package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.TerrainVAO;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.function.Function;

public class PlayerCenteredRenderer extends TerrainVAOMapProgram {
    private final Function<Vector2i, TerrainVAO> chunkLoader;
    public int chunkRenderDistance;
    private Vector2f previousPosition = null;

    public PlayerCenteredRenderer(CameraMatrix cameraMatrix, Function<Vector2i, TerrainVAO> chunkLoader, int chunkRenderDistance) {
        this(cameraMatrix, chunkLoader, chunkRenderDistance, new Vector2i(), new Vector2i());
    }

    public PlayerCenteredRenderer(CameraMatrix cameraMatrix, Function<Vector2i, TerrainVAO> chunkLoader, int chunkRenderDistance, Vector2i skipSrcPos, Vector2i skipSideLength) {
        super(cameraMatrix);
        this.chunkLoader = chunkLoader;
        this.chunkRenderDistance = chunkRenderDistance;

        updateLoadedChunks(skipSrcPos, skipSideLength);
    }

    public void updateLoadedChunks() {
        updateLoadedChunks(new Vector2i(), new Vector2i());
    }

    public void updateLoadedChunks(Vector2i skipSrcPos, Vector2i skipSideLength) {
        updateLoadedChunks(skipSrcPos, skipSideLength, false);
    }

    public void updateLoadedChunks(Vector2i skipSrcPos, Vector2i skipSideLength, boolean force) {
        Vector2f position = new Vector2f();
        position.x = cameraMatrix.position.x - (chunkRenderDistance-1)*64;
        position.y = cameraMatrix.position.z - (chunkRenderDistance-1)*64;
        position.div(64f).floor().mul(64f);

        if (position.equals(previousPosition) && !force)
            return;
        previousPosition = position;

        int sideLength = chunkRenderDistance*2-1;
        Vector2i srcPos = new Vector2i((int) position.x, (int) position.y);


        terrainVAOs.values().removeIf(terrainVAO -> {
            boolean clear = !pointInsideRectangle(terrainVAO.srcPos, srcPos, new Vector2i(sideLength*64))
                    || pointInsideRectangle(terrainVAO.srcPos, skipSrcPos, skipSideLength);
            if (clear)
                terrainVAO.delete();
            return clear;
        });

        for (int x = 0; x < sideLength; x++)
            for (int y= 0; y < sideLength; y++) {
                Vector2i pos = new Vector2i((int) position.x + x*64, (int) position.y + y*64);
                if (!pointInsideRectangle(pos, skipSrcPos, skipSideLength))
                    terrainVAOs.computeIfAbsent(pos, chunkLoader);
            }
    }

    private static boolean pointInsideRectangle(Vector2i point, Vector2i srcPos, Vector2i sideLength) {
        return (point.x >= srcPos.x && point.x < srcPos.x+sideLength.x) && (point.y >= srcPos.y && point.y < srcPos.y+sideLength.y);
    }
}
