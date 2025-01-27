package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Region<C> {
    public final Vector2i coord;
    private final LinkedHashMap<Vector2i, C> chunks = new LinkedHashMap<>();

    public Region(Vector2i coord) {
        this.coord = coord;
    }

    public void addChunk(Vector2i coord, C chunk) {
        C oldChunk = chunks.put(coord, chunk);
        if (oldChunk != null)
            new IllegalStateException("Chunk " + coord.x + ", " + coord.y+ " was overwritten!").printStackTrace();
    }

    public C getChunk(Vector2i coord, Function<Vector2i, C> chunkGenerator) {
        return chunks.computeIfAbsent(coord, chunkGenerator);
    }

    public Set<Map.Entry<Vector2i, C>> getAllChunks() {
        return chunks.entrySet();
    }

    @Override
    public String toString() {
        return "Region{" +
                "coord=" + coord +
                ", chunks=" + chunks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Region<?> region = (Region<?>) o;
        return Objects.equals(coord, region.coord) && Objects.equals(chunks, region.chunks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coord, chunks);
    }
}
