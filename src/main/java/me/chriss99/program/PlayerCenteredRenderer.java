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
        super(cameraMatrix);
        this.chunkLoader = chunkLoader;
        this.chunkRenderDistance = chunkRenderDistance;

        updateLoadedChunks();
    }

    public void updateLoadedChunks() {
        Vector2f position = new Vector2f();
        position.x = cameraMatrix.position.x - (chunkRenderDistance-1)*64;
        position.y = cameraMatrix.position.z - (chunkRenderDistance-1)*64;
        position.div(64f).floor().mul(64f);

        if (position.equals(previousPosition))
            return;
        previousPosition = position;

        int sideLength = chunkRenderDistance*2-1;
        Vector2i srcPos = new Vector2i((int) position.x, (int) position.y);

        terrainVAOs.values().removeIf(terrainVAO -> !pointInsideSquare(terrainVAO.srcPos, srcPos, sideLength*64));
        for (int x = 0; x < sideLength; x++)
            for (int y= 0; y < sideLength; y++)
                terrainVAOs.computeIfAbsent(new Vector2i((int) position.x + x*64, (int) position.y + y*64), chunkLoader);
    }

    private static boolean pointInsideSquare(Vector2i point, Vector2i srcPos, int sideLength) {
        return (point.x >= srcPos.x && point.x < srcPos.x+sideLength) && (point.y >= srcPos.y && point.y < srcPos.y+sideLength);
    }
}
