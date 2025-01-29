package me.chriss99.program;

import me.chriss99.ChunkVAO;
import me.chriss99.CutOutSquareTileLoadManager;
import me.chriss99.worldmanagement.TileMap2D;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class PositionCenteredRenderer<T extends ChunkVAO> {
    protected final RenderProgram<T> renderProgram;
    protected final TileMap2D<T> chunkVaos;
    protected final CutOutSquareTileLoadManager<T> loadManager;

    public final int chunkSize;
    public int chunkRenderDistance;

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, BiFunction<Vector2i, Integer, T> chunkLoader, Vector3f position, int chunkSize, int chunkRenderDistance) {
        this(renderProgram, chunkLoader, position, chunkSize, chunkRenderDistance, new Vector2i(), new Vector2i());
    }

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, BiFunction<Vector2i, Integer, T> chunkLoader, Vector3f position, int chunkSize, int chunkRenderDistance, Vector2i skipSrcPos, Vector2i skipSideLength) {
        this.renderProgram = renderProgram;
        this.loadManager = new CutOutSquareTileLoadManager<>(chunkRenderDistance, new Vector2f(position.x, position.z), skipSrcPos, skipSideLength);
        this.chunkVaos = new TileMap2D<>(key -> chunkLoader.apply(new Vector2i(key).mul(chunkSize), chunkSize), (v, t) -> t.delete(), loadManager);
        this.chunkSize = chunkSize;
        this.chunkRenderDistance = chunkRenderDistance;

        updateLoadedChunks(position, skipSrcPos, skipSideLength);
    }

    public void updateLoadedChunks(Vector3f newPosition) {
        updateLoadedChunks(newPosition, null, null);
    }

    public void updateLoadedChunks(Vector3f newPosition, Vector2i skipSrcPos, Vector2i skipSideLength) {
        loadManager.setPosition(new Vector2f(newPosition.x, newPosition.z).div(chunkSize).floor());
        if (skipSrcPos != null)
            loadManager.setSkipSrcPos(new Vector2i(skipSrcPos).div(chunkSize));
        if (skipSideLength != null)
            loadManager.setSkipSideLength(new Vector2i(skipSideLength).div(chunkSize));
        chunkVaos.manageLoad();
    }

    public void render() {
        renderProgram.render(chunkVaos.getAllTiles());
    }

    public void delete() {
        chunkVaos.unloadAll();
        renderProgram.delete();
    }
}
