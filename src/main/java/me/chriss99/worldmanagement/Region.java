package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Region {
    public final Vector2i coord;
    private final LinkedHashMap<Vector2i, Chunk> chunks = new LinkedHashMap<>();

    public Region(Vector2i coord) {
        this.coord = coord;
    }

    public void addChunk(Vector2i coord, Chunk chunk) {
        Chunk oldChunk = chunks.put(coord, chunk);
        if (oldChunk != null)
            new IllegalStateException("Chunk " + coord.x + ", " + coord.y+ " was overwritten!").printStackTrace();
    }

    public Chunk getChunk(Vector2i coord, Function<Vector2i, Chunk> chunkGenerator) {
        return chunks.computeIfAbsent(coord, chunkGenerator);
    }

    public Set<Map.Entry<Vector2i, Chunk>> getAllChunks() {
        return chunks.entrySet();
    }
}
