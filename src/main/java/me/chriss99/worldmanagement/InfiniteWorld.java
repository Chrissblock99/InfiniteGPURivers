package me.chriss99.worldmanagement;

import me.chriss99.util.Util;
import org.joml.Vector2i;

import java.util.function.BiFunction;

public class InfiniteWorld<T> {
    private final FileBackedTileMap2D<Region<T>> storage;
    private final BiFunction<Vector2i, Integer, T> chunkGenerator;

    public final int chunkSize;
    public final int regionSize;

    public InfiniteWorld(int chunkSize, int regionSize, BiFunction<Vector2i, Integer, T> chunkGenerator, RegionFileManager<T> regionFileManager, TileLoadManager<Region<T>> tileLoadManager) {
        this.storage = new FileBackedTileMap2D<>(regionFileManager::loadFile, regionFileManager, tileLoadManager);
        this.chunkGenerator = chunkGenerator;

        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
    }

    public T getTile(Vector2i pos) {
        return storage.getTile(Util.properIntDivide(pos, regionSize)).getTile(pos, vector2i -> chunkGenerator.apply(vector2i, chunkSize));
    }

    public void unloadAllRegions() {
        storage.unloadAll();
    }
}
