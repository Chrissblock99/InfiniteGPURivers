package me.chriss99.worldmanagement;

import me.chriss99.Util;
import org.joml.Vector2i;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class InfiniteWorld<T> {
    private final Class<T> tClass;

    private final HashMap<Vector2i, Region<T>> loadedRegions = new HashMap<>();
    private final RegionFileManager<T> regionFileManager;
    private final Function<Vector2i, Chunk<T>> chunkGenerator;

    public InfiniteWorld(String worldName, BufferInterpreter<T> bufferInterpreter, Function<Vector2i, Chunk<T>> chunkGenerator) {
        this.tClass = bufferInterpreter.typeClass();
        this.regionFileManager = new RegionFileManager<>(worldName, bufferInterpreter);
        this.chunkGenerator = chunkGenerator;
    }

    public T[][] readArea(int x, int y, int width, int height) {
        @SuppressWarnings("unchecked")
        T[][] array = (T[][]) Array.newInstance(tClass, width, height);
        return readWriteArea(x, y, array, false);
    }

    public void writeArea(int x, int y, T[][] data) {
        readWriteArea(x, y, data, true);
    }

    public T[][] readWriteArea(int x, int y, T[][] data, boolean write) {
        int width = data.length;
        int height = data[0].length;

        int chunkX = Util.properIntDivide(x, 100);
        int chunkY = Util.properIntDivide(y, 100);
        int chunksX = (x+width-1)/100 - chunkX + 1;
        int chunksY = (y+height-1)/100 - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk<T> currentChunk = getChunk(currentChunkX, currentChunkY);

                int currentChunkMinX = (Math.max(currentChunkX*100, x)%100+100)%100;
                int currentChunkMaxX = (Math.min(currentChunkX*100 +99, x+width-1)%100+100)%100;
                int currentChunkMinY = (Math.max(currentChunkY*100, y)%100+100)%100;
                int currentChunkMaxY = (Math.min(currentChunkY*100 +99, y+height-1)%100+100)%100;

                for (int i = currentChunkMinX; i <= currentChunkMaxX; i++) {
                    T[] src = currentChunk.data()[i];
                    int srcPos = currentChunkMinY;
                    T[] dest = data[currentChunkX*100 + i - x];
                    int destPos = currentChunkY*100 + currentChunkMinY - y;

                    if (write) {
                        T[] tempArray = src;
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

    private Region<T> getRegion(Vector2i regionCoord) {
        return loadedRegions.computeIfAbsent(regionCoord, regionFileManager::loadRegion);
    }

    private Chunk<T> getChunk(int x, int y) {
        return getRegion(new Vector2i(Util.properIntDivide(x, 10), Util.properIntDivide(y, 10))).getChunk(new Vector2i(x, y), chunkGenerator);
    }

    public void unloadRegion(Vector2i regionCoord) {
        Region<T> region = loadedRegions.remove(regionCoord);
        if (region == null) {
            System.out.println("Region " + regionCoord.x+ ", " + regionCoord.y + " is not loaded, but was called for unloading!");
            return;
        }

        regionFileManager.saveRegion(region);
    }

    public void unloadAllRegions() {
        for (Map.Entry<Vector2i, Region<T>> entry : loadedRegions.entrySet())
            regionFileManager.saveRegion(entry.getValue());
    }
}
