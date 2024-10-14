package me.chriss99.worldmanagement;

import me.chriss99.Array2DBufferWrapper;
import me.chriss99.Util;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class InfiniteWorld {
    private final HashMap<Vector2i, Region> loadedRegions = new HashMap<>();
    private final RegionFileManager regionFileManager;
    private final BiFunction<Vector2i, Integer, Chunk> chunkGenerator;

    public final int elementSize;
    public final int format;
    public final int type;

    public final int chunkSize;
    public final int regionSize;

    public InfiniteWorld(String worldName, int format, int type, int chunkSize, int regionSize, BiFunction<Vector2i, Integer, Chunk> chunkGenerator) {
        this.regionFileManager = new RegionFileManager(worldName, format, type, chunkSize);
        this.chunkGenerator = chunkGenerator;

        elementSize = Array2DBufferWrapper.sizeOf(format, type);
        this.format = format;
        this.type = type;

        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
    }

    public Array2DBufferWrapper readArea(int x, int y, int width, int height) {
        Array2DBufferWrapper data = new Array2DBufferWrapper(format, type, width, height);

        int chunkX = Util.properIntDivide(x, chunkSize);
        int chunkY = Util.properIntDivide(y, chunkSize);
        int chunksX = Util.properIntDivide(x+width-1, chunkSize) - chunkX + 1;
        int chunksY = Util.properIntDivide(y+height-1, chunkSize) - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk currentChunk = getChunk(currentChunkX, currentChunkY);

                int currentChunkMinX = (Math.max(currentChunkX*chunkSize, x)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMaxX = (Math.min(currentChunkX*chunkSize +chunkSize-1, x+width-1)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMinY = (Math.max(currentChunkY*chunkSize, y)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMaxY = (Math.min(currentChunkY*chunkSize +chunkSize-1, y+height-1)%chunkSize+chunkSize)%chunkSize;

                for (int i = currentChunkMinY; i <= currentChunkMaxY; i++) {
                    Array2DBufferWrapper src = currentChunk.data().slice(i);
                    int srcPos = currentChunkMinX;
                    Array2DBufferWrapper dest = data.slice(currentChunkY*chunkSize + i - y);
                    int destPos = currentChunkX*chunkSize + currentChunkMinX - x;

                    dest.buffer.put(destPos*elementSize, src.buffer, srcPos*elementSize, (currentChunkMaxX-currentChunkMinX + 1)*elementSize);
                }
            }

        return data;
    }

    public void writeArea(int x, int y, Array2DBufferWrapper data) {
        int width = data.width;
        int height = data.height;


        int chunkX = Util.properIntDivide(x, chunkSize);
        int chunkY = Util.properIntDivide(y, chunkSize);
        int chunksX = Util.properIntDivide(x+width-1, chunkSize) - chunkX + 1;
        int chunksY = Util.properIntDivide(y+height-1, chunkSize) - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk currentChunk = getChunk(currentChunkX, currentChunkY);

                int currentChunkMinX = (Math.max(currentChunkX*chunkSize, x)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMaxX = (Math.min(currentChunkX*chunkSize +chunkSize-1, x+width-1)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMinY = (Math.max(currentChunkY*chunkSize, y)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMaxY = (Math.min(currentChunkY*chunkSize +chunkSize-1, y+height-1)%chunkSize+chunkSize)%chunkSize;

                for (int i = currentChunkMinY; i <= currentChunkMaxY; i++) {
                    Array2DBufferWrapper src = data.slice(currentChunkY*chunkSize + i - y);
                    int srcPos = currentChunkX*chunkSize + currentChunkMinX - x;
                    Array2DBufferWrapper dest = currentChunk.data().slice(i);
                    int destPos = currentChunkMinX;

                    dest.buffer.put(destPos*elementSize, src.buffer, srcPos*elementSize, (currentChunkMaxX-currentChunkMinX + 1)*elementSize);
                }
            }
    }

    private Region getRegion(Vector2i regionCoord) {
        return loadedRegions.computeIfAbsent(regionCoord, regionFileManager::loadRegion);
    }

    private Chunk getChunk(int x, int y) {
        return getRegion(new Vector2i(Util.properIntDivide(x, regionSize), Util.properIntDivide(y, regionSize))).getChunk(new Vector2i(x, y), vector2i -> chunkGenerator.apply(vector2i, chunkSize));
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
