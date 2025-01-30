package me.chriss99.worldmanagement;

import me.chriss99.Array2DBufferWrapper;
import me.chriss99.Util;
import org.joml.Vector2i;

import java.util.function.BiFunction;

public class InfiniteChunkWorld extends InfiniteWorld<Chunk> {
    public final Array2DBufferWrapper.Type type;

    public InfiniteChunkWorld(String worldName, Array2DBufferWrapper.Type type, int chunkSize, int regionSize, BiFunction<Vector2i, Integer, Chunk> chunkGenerator, TileLoadManager<Region<Chunk>> tileLoadManager) {
        super(chunkSize, regionSize, chunkGenerator, new ChunkRegionFileManager(worldName, type, chunkSize), tileLoadManager);
        this.type = type;
    }

    public Array2DBufferWrapper readArea(int x, int y, int width, int height) {
        Array2DBufferWrapper data = Array2DBufferWrapper.of(type, width, height);

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

                    dest.buffer.put(destPos*type.elementSize, src.buffer, srcPos*type.elementSize, (currentChunkMaxX-currentChunkMinX + 1)*type.elementSize);
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

                    dest.buffer.put(destPos*type.elementSize, src.buffer, srcPos*type.elementSize, (currentChunkMaxX-currentChunkMinX + 1)*type.elementSize);
                }
            }
    }
}
