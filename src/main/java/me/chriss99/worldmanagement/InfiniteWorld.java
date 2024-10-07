package me.chriss99.worldmanagement;

import me.chriss99.Float2DBufferWrapper;
import me.chriss99.Util;
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

    public Float2DBufferWrapper readArea(int x, int y, int width, int height) {
        float[][] data = new float[width][height];

        int chunkX = Util.properIntDivide(x, 100);
        int chunkY = Util.properIntDivide(y, 100);
        int chunksX = Util.properIntDivide(x+width-1, 100) - chunkX + 1;
        int chunksY = Util.properIntDivide(y+height-1, 100) - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk currentChunk = getChunk(currentChunkX, currentChunkY);

                int currentChunkMinX = (Math.max(currentChunkX*100, x)%100+100)%100;
                int currentChunkMaxX = (Math.min(currentChunkX*100 +99, x+width-1)%100+100)%100;
                int currentChunkMinY = (Math.max(currentChunkY*100, y)%100+100)%100;
                int currentChunkMaxY = (Math.min(currentChunkY*100 +99, y+height-1)%100+100)%100;

                for (int i = currentChunkMinX; i <= currentChunkMaxX; i++) {
                    Float2DBufferWrapper wrapper = new Float2DBufferWrapper(currentChunk.data().buffer, 100, 100);
                    float[] src = wrapper.getArray()[i];
                    int srcPos = currentChunkMinY;
                    float[] dest = data[currentChunkX*100 + i - x];
                    int destPos = currentChunkY*100 + currentChunkMinY - y;

                    System.arraycopy(src, srcPos, dest, destPos, currentChunkMaxY-currentChunkMinY + 1);
                }
            }

        return new Float2DBufferWrapper(data);
    }

    /*public void writeArea(int x, int y, Float2DBufferWrapper data) {
        int width = data.width;
        int height = data.height;
        float[][] dataArray = data.getArray();


        int chunkX = Util.properIntDivide(x, 100);
        int chunkY = Util.properIntDivide(y, 100);
        int chunksX = Util.properIntDivide(x+width-1, 100) - chunkX + 1;
        int chunksY = Util.properIntDivide(y+height-1, 100) - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk currentChunk = getChunk(currentChunkX, currentChunkY);

                int currentChunkMinX = (Math.max(currentChunkX*100, x)%100+100)%100;
                int currentChunkMaxX = (Math.min(currentChunkX*100 +99, x+width-1)%100+100)%100;
                int currentChunkMinY = (Math.max(currentChunkY*100, y)%100+100)%100;
                int currentChunkMaxY = (Math.min(currentChunkY*100 +99, y+height-1)%100+100)%100;

                for (int i = currentChunkMinX; i <= currentChunkMaxX; i++) {
                    float[] src = dataArray[currentChunkX*100 + i - x];
                    int srcPos = currentChunkY*100 + currentChunkMinY - y;
                    float[] dest = currentChunk.data()[i];
                    int destPos = currentChunkMinY;

                    System.arraycopy(src, srcPos, dest, destPos, currentChunkMaxY-currentChunkMinY + 1);
                }
            }
    }*/

    private Region getRegion(Vector2i regionCoord) {
        return loadedRegions.computeIfAbsent(regionCoord, regionFileManager::loadRegion);
    }

    private Chunk getChunk(int x, int y) {
        return getRegion(new Vector2i(Util.properIntDivide(x, 10), Util.properIntDivide(y, 10))).getChunk(new Vector2i(x, y), chunkGenerator);
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
