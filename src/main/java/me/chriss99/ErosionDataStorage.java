package me.chriss99;

import me.chriss99.worldmanagement.*;
import me.chriss99.worldmanagement.iteration.IterableWorld;

import static me.chriss99.Array2DBufferWrapper.Type;

public class ErosionDataStorage {
    public final int chunkSize;
    public final int regionSize;
    public final int iterationChunkSize;
    public final int iterationRegionSize;

    private final TerrainGenerator terrainGenerator;

    public final MipMappedInfiniteChunkWorld mipMappedTerrain;
    public final MipMappedInfiniteChunkWorld mipMappedWater;

    public final InfiniteChunkWorld terrain;
    public final InfiniteChunkWorld water;
    public final InfiniteChunkWorld sediment;
    public final InfiniteChunkWorld hardness;

    public final InfiniteChunkWorld waterOutflow;
    public final InfiniteChunkWorld sedimentOutflow;

    public final InfiniteChunkWorld thermalOutflow1;
    public final InfiniteChunkWorld thermalOutflow2;

    public final IterableWorld iterationInfo;

    public final TileLoadManager tileLoadManager = new LeakingTileLoadManager<>();

    public ErosionDataStorage(String worldName, int chunkSize, int regionSize, int iterationChunkSize, int iterationRegionSize) {
        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
        this.iterationChunkSize = iterationChunkSize;
        this.iterationRegionSize = iterationRegionSize;
        terrainGenerator = new TerrainGenerator(chunkSize);

        mipMappedTerrain = new MipMappedInfiniteChunkWorld(worldName + "/terrain", chunkSize, regionSize, terrainGenerator::generateChunk, i -> tileLoadManager);
        mipMappedWater = new MipMappedInfiniteChunkWorld(worldName + "/water", chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)), i -> tileLoadManager);

        terrain = mipMappedTerrain.getMipMapLevel(0);
        water = mipMappedWater.getMipMapLevel(0);
        sediment = new InfiniteChunkWorld(worldName + "/sediment", Type.FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)), tileLoadManager);
        hardness = new InfiniteChunkWorld(worldName + "/hardness", Type.FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1, 1)), tileLoadManager);

        waterOutflow = new InfiniteChunkWorld(worldName + "/waterOutflow", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)), tileLoadManager);
        sedimentOutflow = new InfiniteChunkWorld(worldName + "/sedimentOutflow", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)), tileLoadManager);

        thermalOutflow1 = new InfiniteChunkWorld(worldName + "/thermalOutflow1", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)), tileLoadManager);
        thermalOutflow2 = new InfiniteChunkWorld(worldName + "/thermalOutflow2", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)), tileLoadManager);

        iterationInfo = new IterableWorld(worldName + "/iteration", iterationChunkSize, iterationRegionSize, tileLoadManager);
    }

    public void unloadAll() {
        mipMappedTerrain.unloadAll();
        mipMappedWater.unloadAll();

        sediment.unloadAllRegions();
        hardness.unloadAllRegions();

        waterOutflow.unloadAllRegions();
        sedimentOutflow.unloadAllRegions();

        thermalOutflow1.unloadAllRegions();
        thermalOutflow2.unloadAllRegions();

        iterationInfo.unloadAllRegions();
    }

    public void cleanGL() {
        terrainGenerator.delete();
    }
}
