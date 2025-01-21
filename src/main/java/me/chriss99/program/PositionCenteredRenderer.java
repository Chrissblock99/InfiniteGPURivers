package me.chriss99.program;

import me.chriss99.ChunkVAO;
import me.chriss99.worldmanagement.TileMap2D;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

public class PositionCenteredRenderer<T extends ChunkVAO> {
    protected final RenderProgram<T> renderProgram;
    protected final TileMap2D<T> chunkVaos;

    public final int chunkSize;
    public int chunkRenderDistance;

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, BiFunction<Vector2i, Integer, T> chunkLoader, Vector3f position, int chunkSize, int chunkRenderDistance) {
        this(renderProgram, chunkLoader, position, chunkSize, chunkRenderDistance, new Vector2i(), new Vector2i());
    }

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, BiFunction<Vector2i, Integer, T> chunkLoader, Vector3f position, int chunkSize, int chunkRenderDistance, Vector2i skipSrcPos, Vector2i skipSideLength) {
        this.renderProgram = renderProgram;
        this.chunkVaos = new TileMap2D<>(srcPos -> chunkLoader.apply(srcPos, chunkSize), this::manageChunks);
        this.chunkSize = chunkSize;
        this.chunkRenderDistance = chunkRenderDistance;

        updateLoadedChunks(position, skipSrcPos, skipSideLength);
    }

    public void updateLoadedChunks(Vector3f newPosition) {
        updateLoadedChunks(newPosition, new Vector2i(), new Vector2i());
    }

    public void updateLoadedChunks(Vector3f newPosition, Vector2i skipSrcPos, Vector2i skipSideLength) {
        updateLoadedChunks(newPosition, skipSrcPos, skipSideLength, false);
    }

    public void updateLoadedChunks(Vector3f newPosition, Vector2i skipSrcPos, Vector2i skipSideLength, boolean force) {
        this.newPosition = newPosition;
        this.skipSrcPos = skipSrcPos;
        this.skipSideLength = skipSideLength;
        this.force = force;

        chunkVaos.runLoadManager();
    }

    private Vector2f previousPosition = null;
    private Vector3f newPosition = null;
    private Vector2i skipSrcPos = null;
    private Vector2i skipSideLength = null;
    private boolean force = false;


    private void manageChunks(HashMap<Vector2i, T> loadedChunks) {
        Vector2f position = new Vector2f();
        position.x = newPosition.x - (chunkRenderDistance-1)*chunkSize;
        position.y = newPosition.z - (chunkRenderDistance-1)*chunkSize;
        position.div(chunkSize).floor().mul(chunkSize);

        if (position.equals(previousPosition) && !force)
            return;
        previousPosition = position;

        int sideLength = chunkRenderDistance*2-1;
        Vector2i srcPos = new Vector2i((int) position.x, (int) position.y);


        loadedChunks.values().removeIf(chunkVAO -> {
            boolean clear = !pointInsideRectangle(chunkVAO.getSrcPos(), srcPos, new Vector2i(sideLength*chunkSize))
                    || pointInsideRectangle(chunkVAO.getSrcPos(), skipSrcPos, skipSideLength);
            if (clear)
                chunkVAO.delete();
            return clear;
        });

        for (int x = 0; x < sideLength; x++)
            for (int y= 0; y < sideLength; y++) {
                Vector2i pos = new Vector2i((int) position.x + x*chunkSize, (int) position.y + y*chunkSize);
                if (!pointInsideRectangle(pos, skipSrcPos, skipSideLength))
                    chunkVaos.getTile(pos);
            }
    }

    private static boolean pointInsideRectangle(Vector2i point, Vector2i srcPos, Vector2i sideLength) {
        return (point.x >= srcPos.x && point.x < srcPos.x+sideLength.x) && (point.y >= srcPos.y && point.y < srcPos.y+sideLength.y);
    }

    public void render() {
        renderProgram.render(chunkVaos.getAllTiles());
    }

    public void delete() {
        for (ChunkVAO vao : chunkVaos.getAllTiles())
            vao.delete();
        renderProgram.delete();
    }
}
