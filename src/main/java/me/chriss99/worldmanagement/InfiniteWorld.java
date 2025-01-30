package me.chriss99.worldmanagement;

import me.chriss99.Util;
import org.joml.Vector2i;

import java.util.function.BiFunction;

public class InfiniteWorld<C> {
    private final FileBackedTileMap2D<Region<C>> storage;
    private final BiFunction<Vector2i, Integer, C> chunkGenerator;

    public final int chunkSize;
    public final int regionSize;

    public InfiniteWorld(int chunkSize, int regionSize, BiFunction<Vector2i, Integer, C> chunkGenerator, RegionFileManager<C> regionFileManager, TileLoadManager<Region<C>> tileLoadManager) {
        this.storage = new FileBackedTileMap2D<>(regionFileManager::loadFile, regionFileManager, tileLoadManager);
        this.chunkGenerator = chunkGenerator;

        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
    }

    public C getChunk(int x, int y) {
        return storage.getTile(new Vector2i(Util.properIntDivide(x, regionSize), Util.properIntDivide(y, regionSize))).getChunk(new Vector2i(x, y), vector2i -> chunkGenerator.apply(vector2i, chunkSize));
    }

    public void unloadAllRegions() {
        storage.unloadAll();
    }
}
