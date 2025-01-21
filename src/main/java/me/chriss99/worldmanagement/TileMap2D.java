package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class TileMap2D<T> {
    protected final HashMap<Vector2i, T> loadedTiles = new LinkedHashMap<>();
    protected final Function<Vector2i, T> tileGenerator;
    protected final Consumer<HashMap<Vector2i, T>> loadManager;

    public TileMap2D(Function<Vector2i, T> tileGenerator, Consumer<HashMap<Vector2i, T>> loadManager) {
        this.tileGenerator = tileGenerator;
        this.loadManager = loadManager;
    }

    public T getTile(Vector2i coord) {
        return loadedTiles.computeIfAbsent(coord, tileGenerator);
    }

    public Collection<T> getAllTiles() {
        return ((HashMap<Vector2i, T>) loadedTiles.clone()).values();
    }

    public void runLoadManager() {
        loadManager.accept(loadedTiles);
    }

    public void unloadAll() {
        loadedTiles.clear();
    }
}
