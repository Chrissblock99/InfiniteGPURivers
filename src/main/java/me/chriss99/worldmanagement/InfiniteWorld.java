package me.chriss99.worldmanagement;

import me.chriss99.Util;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class InfiniteWorld<C> {
    private final HashMap<Vector2i, Region<C>> loadedRegions = new HashMap<>();
    private final RegionFileManager<C> regionFileManager;
    private final BiFunction<Vector2i, Integer, C> chunkGenerator;

    public final int chunkSize;
    public final int regionSize;

    public InfiniteWorld(int chunkSize, int regionSize, BiFunction<Vector2i, Integer, C> chunkGenerator, RegionFileManager<C> regionFileManager) {
        this.regionFileManager = regionFileManager;
        this.chunkGenerator = chunkGenerator;

        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
    }

    private Region<C> getRegion(Vector2i regionCoord) {
        return loadedRegions.computeIfAbsent(regionCoord, regionFileManager::loadRegion);
    }

    public C getChunk(int x, int y) {
        return getRegion(new Vector2i(Util.properIntDivide(x, regionSize), Util.properIntDivide(y, regionSize))).getChunk(new Vector2i(x, y), vector2i -> chunkGenerator.apply(vector2i, chunkSize));
    }

    public void unloadRegion(Vector2i regionCoord) {
        Region<C> region = loadedRegions.remove(regionCoord);
        if (region == null) {
            System.out.println("Region " + regionCoord.x+ ", " + regionCoord.y + " is not loaded, but was called for unloading!");
            return;
        }

        regionFileManager.saveRegion(region);
    }

    public void unloadAllRegions() {
        for (Map.Entry<Vector2i, Region<C>> entry : loadedRegions.entrySet())
            regionFileManager.saveRegion(entry.getValue());
    }
}
