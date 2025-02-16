package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a map of Tiles, guarantees that ANY Vector2i can be asked for and stores previously loaded Tiles <br>
 * loading and unloading behaviour can be augmented with the TileLoadManager
 *
 * @param <T> Type of the Tiles to be managed
 */
public class TileMap2D<T> {
    protected final HashMap<Vector2i, T> loadedTiles = new LinkedHashMap<>();
    protected final Function<Vector2i, T> tileLoader;
    protected final BiConsumer<Vector2i, T> tileUnloader;
    protected TileLoadManager<T> loadManager;

    public TileMap2D(Function<Vector2i, T> tileLoader, BiConsumer<Vector2i, T> tileUnloader, TileLoadManager<T> loadManager) {
        this.tileLoader = tileLoader;
        this.tileUnloader = tileUnloader;
        this.loadManager = loadManager;
    }

    public T getTile(Vector2i coord) {
        return loadedTiles.computeIfAbsent(coord, tileLoader);
    }

    public Collection<T> getAllTiles() {
        return ((HashMap<Vector2i, T>) loadedTiles.clone()).values();
    }

    public void reloadAll() {
        loadedTiles.replaceAll((k, v) -> tileLoader.apply(k));
    }

    public void manageLoad() {
        HashSet<Vector2i> toRemove = new HashSet<>();

        loadedTiles.forEach((v, t) -> {
            if (!loadManager.loadPolicy(v, t))
                toRemove.add(v);
        });

        for (Vector2i v : loadManager.loadCommander())
            if (!toRemove.remove(v))
                getTile(v);

        for (Vector2i v : toRemove)
            tileUnloader.accept(v, loadedTiles.remove(v));
    }

    public void unloadAll() {
        loadedTiles.forEach(tileUnloader);
        loadedTiles.clear();
    }
}
