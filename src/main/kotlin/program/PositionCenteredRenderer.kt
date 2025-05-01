package me.chriss99.program;

import me.chriss99.Area;
import me.chriss99.ChunkVAO;
import me.chriss99.CutOutRectangleTLM;
import me.chriss99.worldmanagement.TileMap2D;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class PositionCenteredRenderer<T extends ChunkVAO> {
    protected final RenderProgram<T> renderProgram;
    protected final TileMap2D<T> chunkVaos;
    protected final CutOutRectangleTLM<T> loadManager;

    public final int chunkSize;
    public int chunkRenderDistance;

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, BiFunction<Vector2i, Integer, T> chunkLoader, Vector3f position, int chunkSize, int chunkRenderDistance) {
        this(renderProgram, chunkLoader, position, chunkSize, chunkRenderDistance, new Area());
    }

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, BiFunction<Vector2i, Integer, T> chunkLoader, Vector3f position, int chunkSize, int chunkRenderDistance, Area skipArea) {
        this.renderProgram = renderProgram;
        this.loadManager = new CutOutRectangleTLM<>(chunkRenderDistance, new Vector2f(position.x, position.z), skipArea);
        this.chunkVaos = new TileMap2D<>(key -> chunkLoader.apply(new Vector2i(key).mul(chunkSize), chunkSize), (v, t) -> t.delete(), loadManager);
        this.chunkSize = chunkSize;
        this.chunkRenderDistance = chunkRenderDistance;

        updateLoadedChunks(position, skipArea);
    }

    public void updateLoadedChunks(Vector3f newPosition) {
        updateLoadedChunks(newPosition, null);
    }

    public void updateLoadedChunks(Vector3f newPosition, Area skipArea) {
        loadManager.setPosition(new Vector2f(newPosition.x, newPosition.z).div(chunkSize).floor());
        if (skipArea != null)
            loadManager.setSkipArea(skipArea.div(chunkSize));
        chunkVaos.manageLoad();
    }

    public void reloadAll() {
        chunkVaos.reloadAll();
    }

    public int getChunkRenderDistance() {
        return loadManager.getRenderDistance();
    }

    public void setChunkRenderDistance(int renderDistance) {
        loadManager.setRenderDistance(renderDistance);
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
