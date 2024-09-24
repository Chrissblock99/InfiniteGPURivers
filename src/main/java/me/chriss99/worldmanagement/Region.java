package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Region<T> {
    public final Vector2i coord;
    private final HashMap<Vector2i, Chunk<T>> chunks = new HashMap<>();

    public Region(Vector2i coord) {
        this.coord = coord;
    }

    public void addChunk(Vector2i coord, Chunk<T> chunk) {
        Chunk<T> oldChunk = chunks.put(coord, chunk);
        if (oldChunk != null)
            new IllegalStateException("Chunk " + coord.x + ", " + coord.y+ " was overwritten!").printStackTrace();
    }

    public Chunk<T> getChunk(Vector2i coord, Function<Vector2i, Chunk<T>> chunkGenerator) {
        return chunks.computeIfAbsent(coord, chunkGenerator);
    }

    public Set<Map.Entry<Vector2i, Chunk<T>>> getAllChunks() {
        return chunks.entrySet();
    }
}
