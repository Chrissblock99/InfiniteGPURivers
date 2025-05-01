package me.chriss99.worldmanagement;

import me.chriss99.Area;
import me.chriss99.Array2DBufferWrapper;
import me.chriss99.util.Util;
import org.joml.Vector2i;

import java.util.function.BiFunction;

public class InfiniteChunkWorld extends InfiniteWorld<Chunk> {
    public final Array2DBufferWrapper.Type type;

    public InfiniteChunkWorld(String worldName, Array2DBufferWrapper.Type type, int chunkSize, int regionSize, BiFunction<Vector2i, Integer, Chunk> chunkGenerator, TileLoadManager<Region<Chunk>> tileLoadManager) {
        super(chunkSize, regionSize, chunkGenerator, new ChunkRegionFileManager(worldName, type, chunkSize), tileLoadManager);
        this.type = type;
    }

    public Array2DBufferWrapper readArea(Area area) {
        return readWriteArea(area.srcPos(), Array2DBufferWrapper.of(type, area.getSize()), true);
    }

    public void writeArea(Vector2i pos, Array2DBufferWrapper data) {
        readWriteArea(pos, data, false);
    }

    private Array2DBufferWrapper readWriteArea(Vector2i pos, Array2DBufferWrapper data, boolean read) {
        int x = pos.x;
        int y = pos.y;
        int width = data.getSize().x;
        int height = data.getSize().y;


        int chunkX = Util.properIntDivide(x, chunkSize);
        int chunkY = Util.properIntDivide(y, chunkSize);
        int chunksX = Util.properIntDivide(x+width-1, chunkSize) - chunkX + 1;
        int chunksY = Util.properIntDivide(y+height-1, chunkSize) - chunkY + 1;

        for (int currentChunkX = chunkX; currentChunkX < chunkX+chunksX; currentChunkX++)
            for (int currentChunkY = chunkY; currentChunkY < chunkY+chunksY; currentChunkY++) {
                Chunk currentChunk = getTile(new Vector2i(currentChunkX, currentChunkY));

                int currentChunkMinX = (Math.max(currentChunkX*chunkSize, x)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMaxX = (Math.min(currentChunkX*chunkSize +chunkSize-1, x+width-1)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMinY = (Math.max(currentChunkY*chunkSize, y)%chunkSize+chunkSize)%chunkSize;
                int currentChunkMaxY = (Math.min(currentChunkY*chunkSize +chunkSize-1, y+height-1)%chunkSize+chunkSize)%chunkSize;

                for (int i = currentChunkMinY; i <= currentChunkMaxY; i++) {
                    Array2DBufferWrapper src = data.slice(currentChunkY*chunkSize + i - y);
                    int srcPos = currentChunkX*chunkSize + currentChunkMinX - x;
                    Array2DBufferWrapper dest = currentChunk.data().slice(i);
                    int destPos = currentChunkMinX;

                    if (read) {
                        Array2DBufferWrapper temp = dest;
                        dest = src;
                        src = temp;

                        int tempPos = destPos;
                        destPos = srcPos;
                        srcPos = tempPos;
                    }

                    dest.buffer.put(destPos*type.elementSize, src.buffer, srcPos*type.elementSize, (currentChunkMaxX-currentChunkMinX + 1)*type.elementSize);
                }
            }

        return data;
    }
}
