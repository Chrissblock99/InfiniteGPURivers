package me.chriss99.worldmanagement;

import me.chriss99.Array2DBufferWrapper;
import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;

public class MipMappedInfiniteChunkWorld {
    private final LinkedHashMap<Integer, InfiniteChunkWorld> mipmaps = new LinkedHashMap<>();

    private final String worldName;
    private final int chunkSize;
    private final int regionSize;

    private final BiFunction<Vector2i, Integer, Chunk> chunkGenerator;

    public MipMappedInfiniteChunkWorld(String worldName, int chunkSize, int regionSize, BiFunction<Vector2i, Integer, Chunk> chunkGenerator) {
        this.worldName = worldName;
        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
        this.chunkGenerator = chunkGenerator;
    }

    public InfiniteChunkWorld getMipMapLevel(int i) {
        return mipmaps.computeIfAbsent(i, (ignored) ->
                new InfiniteChunkWorld(worldName + "/mm" + i, Array2DBufferWrapper.Type.FLOAT, chunkSize, regionSize, (srcPos, chunkSize) -> mipMapChunk(i, srcPos, chunkSize)));
    }

    private Chunk mipMapChunk(int mipMapLevel, Vector2i srcPos, int chunkSize) {
        if (mipMapLevel == 0)
            return chunkGenerator.apply(srcPos, chunkSize);

        Array2DBufferWrapper mipMapFrom = getMipMapLevel(mipMapLevel-1).readArea(srcPos.x*2, srcPos.y*2, chunkSize*2, chunkSize*2);
        return new Chunk(mipMapFrom.mipMap());
    }

    public void unloadAll() {
        for (InfiniteChunkWorld infiniteWorld : mipmaps.values())
            infiniteWorld.unloadAllRegions();
    }
}
