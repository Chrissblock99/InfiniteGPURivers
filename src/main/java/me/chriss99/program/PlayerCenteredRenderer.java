package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.TerrainVAO;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.function.Function;

public class PlayerCenteredRenderer extends TerrainVAOMapProgram {
    private final Vector2f playerPosition = new Vector2f();
    private final Function<Vector2i, TerrainVAO> chunkLoader;
    private float renderDistance;

    public PlayerCenteredRenderer(CameraMatrix cameraMatrix, Function<Vector2i, TerrainVAO> chunkLoader, int renderDistance) {
        super(cameraMatrix);
        this.chunkLoader = chunkLoader;
        this.renderDistance = renderDistance;

        updatePlayerPosition();
    }

    public void updatePlayerPosition() {
        this.playerPosition.x = cameraMatrix.position.x;
        this.playerPosition.y = cameraMatrix.position.z;

        updateLoadedChunks();
    }

    public void updateLoadedChunks() {
        playerPosition.div(64f).floor().mul(64f);

        //terrainVAOs.clear();
        terrainVAOs.computeIfAbsent(new Vector2i((int) playerPosition.x, (int) playerPosition.y), chunkLoader);
    }
}
