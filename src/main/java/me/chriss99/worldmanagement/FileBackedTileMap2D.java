package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class FileBackedTileMap2D<T> extends TileMap2D<T> {
    protected final FileStorage<Vector2i, T> fileStorage;

    public FileBackedTileMap2D(Function<Vector2i, T> tileGenerator, Consumer<HashMap<Vector2i, T>> loadPolicyManager, FileStorage<Vector2i, T> fileStorage) {
        super(tileGenerator, loadPolicyManager);
        this.fileStorage = fileStorage;
    }

    @Override
    public T getTile(Vector2i coord) {
        return loadedTiles.computeIfAbsent(coord, v -> {
            if (fileStorage.hasFile(v))
                return fileStorage.loadFile(v);
            return tileGenerator.apply(v);
        });
    }

    @Override
    public void runLoadManager() {
        Collection<T> before = ((HashMap<Vector2i, T>) loadedTiles.clone()).values();
        super.runLoadManager();
        before.removeAll(loadedTiles.values());

        before.forEach(fileStorage::saveFile);
    }

    @Override
    public void unloadAll() {
        loadedTiles.values().forEach(fileStorage::saveFile);
        super.unloadAll();
    }
}
