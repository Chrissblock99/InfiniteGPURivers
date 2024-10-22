package me.chriss99.program;

import me.chriss99.ChunkVAO;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class PositionCenteredRenderer<T extends ChunkVAO> {
    protected final RenderProgram<T> renderProgram;
    protected final LinkedHashMap<Vector2i, T> chunkVAOS = new LinkedHashMap<>();
    private final Function<Vector2i, T> chunkLoader;

    public int chunkRenderDistance;
    private Vector2f previousPosition = null;

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, Function<Vector2i, T> chunkLoader, Vector3f position, int chunkRenderDistance) {
        this(renderProgram, chunkLoader, position, chunkRenderDistance, new Vector2i(), new Vector2i());
    }

    public PositionCenteredRenderer(RenderProgram<T> renderProgram, Function<Vector2i, T> chunkLoader, Vector3f position, int chunkRenderDistance, Vector2i skipSrcPos, Vector2i skipSideLength) {
        this.renderProgram = renderProgram;
        this.chunkLoader = chunkLoader;
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
        Vector2f position = new Vector2f();
        position.x = newPosition.x - (chunkRenderDistance-1)*64;
        position.y = newPosition.z - (chunkRenderDistance-1)*64;
        position.div(64f).floor().mul(64f);

        if (position.equals(previousPosition) && !force)
            return;
        previousPosition = position;

        int sideLength = chunkRenderDistance*2-1;
        Vector2i srcPos = new Vector2i((int) position.x, (int) position.y);


        chunkVAOS.values().removeIf(chunkVAO -> {
            boolean clear = !pointInsideRectangle(chunkVAO.getSrcPos(), srcPos, new Vector2i(sideLength*64))
                    || pointInsideRectangle(chunkVAO.getSrcPos(), skipSrcPos, skipSideLength);
            if (clear)
                chunkVAO.delete();
            return clear;
        });

        for (int x = 0; x < sideLength; x++)
            for (int y= 0; y < sideLength; y++) {
                Vector2i pos = new Vector2i((int) position.x + x*64, (int) position.y + y*64);
                if (!pointInsideRectangle(pos, skipSrcPos, skipSideLength))
                    chunkVAOS.computeIfAbsent(pos, chunkLoader);
            }
    }

    private static boolean pointInsideRectangle(Vector2i point, Vector2i srcPos, Vector2i sideLength) {
        return (point.x >= srcPos.x && point.x < srcPos.x+sideLength.x) && (point.y >= srcPos.y && point.y < srcPos.y+sideLength.y);
    }

    public void render() {
        renderProgram.render(chunkVAOS.values());
    }

    public void delete() {
        for (ChunkVAO vao : chunkVAOS.values())
            vao.delete();
        renderProgram.delete();
    }
}
