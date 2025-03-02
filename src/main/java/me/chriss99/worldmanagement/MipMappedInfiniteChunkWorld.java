package me.chriss99.worldmanagement;

import me.chriss99.Area;
import me.chriss99.Array2DBufferWrapper;
import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.function.BiFunction;

public class MipMappedInfiniteChunkWorld {
    private final LinkedHashMap<Integer, InfiniteChunkWorld> mipmaps = new LinkedHashMap<>();

    private final String worldName;
    private final int chunkSize;
    private final int regionSize;

    private final BiFunction<Vector2i, Integer, Chunk> chunkGenerator;
    private final Function<Integer, TileLoadManager<Region<Chunk>>> tileLoadManagerSupplier;

    public MipMappedInfiniteChunkWorld(String worldName, int chunkSize, int regionSize, BiFunction<Vector2i, Integer, Chunk> chunkGenerator, Function<Integer, TileLoadManager<Region<Chunk>>> tileLoadManagerSupplier) {
        this.worldName = worldName;
        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
        this.chunkGenerator = chunkGenerator;
        this.tileLoadManagerSupplier = tileLoadManagerSupplier;
    }

    public InfiniteChunkWorld getMipMapLevel(int i) {
        return mipmaps.computeIfAbsent(i, (ignored) ->
                new InfiniteChunkWorld(worldName + "/mm" + i, Array2DBufferWrapper.Type.FLOAT, chunkSize, regionSize, (srcPos, chunkSize) -> mipMapChunk(i, srcPos, chunkSize), tileLoadManagerSupplier.apply(i)));
    }

    private Chunk mipMapChunk(int mipMapLevel, Vector2i srcPos, int chunkSize) {
        if (mipMapLevel == 0)
            return chunkGenerator.apply(srcPos, chunkSize);

        Array2DBufferWrapper mipMapFrom = getMipMapLevel(mipMapLevel-1).readArea(new Area(srcPos, chunkSize).mul(2));
        return new Chunk(mipMapFrom.mipMap());
    }

    public void unloadAll() {
        for (InfiniteChunkWorld infiniteWorld : mipmaps.values())
            infiniteWorld.unloadAllRegions();
    }
}
