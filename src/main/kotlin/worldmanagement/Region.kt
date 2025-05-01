package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Region<T> {
    public final Vector2i coord;
    private final LinkedHashMap<Vector2i, T> tiles = new LinkedHashMap<>();

    public Region(Vector2i coord) {
        this.coord = coord;
    }

    public void addChunk(Vector2i coord, T chunk) {
        T oldTile = tiles.put(coord, chunk);
        if (oldTile != null)
            new IllegalStateException("Tile " + coord.x + ", " + coord.y+ " was overwritten!").printStackTrace();
    }

    public T getTile(Vector2i coord, Function<Vector2i, T> tileGenerator) {
        return tiles.computeIfAbsent(coord, tileGenerator);
    }

    public Set<Map.Entry<Vector2i, T>> getAllTiles() {
        return tiles.entrySet();
    }

    @Override
    public String toString() {
        return "Region{" +
                "coord=" + coord +
                ", tiles=" + tiles +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Region<?> region = (Region<?>) o;
        return Objects.equals(coord, region.coord) && Objects.equals(tiles, region.tiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coord, tiles);
    }
}
