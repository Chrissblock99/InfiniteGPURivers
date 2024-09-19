package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class InfiniteWorld {
    private final HashMap<Vector2i, Region> loadedRegions = new HashMap<>();
    private final RegionFileManager regionFileManager;
    private final Function<Vector2i, Chunk> chunkGenerator;

    public InfiniteWorld(String worldName, Function<Vector2i, Chunk> chunkGenerator) {
        this.regionFileManager = new RegionFileManager(worldName);
        this.chunkGenerator = chunkGenerator;
    }

    public int[][] readArea(int x, int y, int width, int height) {
        return readWriteArea(x, y, new int[width][height], false);
    }

    public void writeArea(int x, int y, int[][] data) {
        readWriteArea(x, y, data, true);
    }

    public int[][] readWriteArea(int x, int y, int[][] data, boolean write) {
        int width = data.length;
        int height = data[0].length;

        int chunkX = x/100;
        int chunkY = y/100;
        int chunksX = (x+width-1)/100 - chunkX + 1;
        int chunksY = (y+height-1)/100 - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk currentChunk = getChunk(currentChunkX, currentChunkY);

                int currentChunkMinX = Math.max(currentChunkX*100, x)%100;
                int currentChunkMaxX = Math.min(currentChunkX*100 +99, x+width-1)%100;
                int currentChunkMinY = Math.max(currentChunkY*100, y)%100;
                int currentChunkMaxY = Math.min(currentChunkY*100 +99, y+height-1)%100;

                for (int i = currentChunkMinX; i <= currentChunkMaxX; i++) {
                    int[] src = currentChunk.data()[i];
                    int srcPos = currentChunkMinY;
                    int[] dest = data[currentChunkX*100 + i - x];
                    int destPos = currentChunkY*100 + currentChunkMinY - y;

                    if (write) {
                        int[] tempArray = src;
                        src = dest;
                        dest = tempArray;

                        int tempInt = srcPos;
                        srcPos = destPos;
                        destPos = tempInt;
                    }

                    System.arraycopy(src, srcPos, dest, destPos, currentChunkMaxY-currentChunkMinY + 1);
                }
            }

        return data;
    }

    private Region getRegion(Vector2i regionCoord) {
        return loadedRegions.computeIfAbsent(regionCoord, regionFileManager::loadRegion);
    }

    private Chunk getChunk(int x, int y) {
        return getRegion(new Vector2i(x, y).div(10)).getChunk(new Vector2i(x, y), chunkGenerator);
    }

    public void unloadRegion(Vector2i regionCoord) {
        Region region = loadedRegions.remove(regionCoord);
        if (region == null) {
            System.out.println("Region " + regionCoord.x+ ", " + regionCoord.y + " is not loaded, but was called for unloading!");
            return;
        }

        regionFileManager.saveRegion(region);
    }

    public void unloadAllRegions() {
        for (Map.Entry<Vector2i, Region> entry : loadedRegions.entrySet())
            regionFileManager.saveRegion(entry.getValue());
    }
}