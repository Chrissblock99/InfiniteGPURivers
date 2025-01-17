package me.chriss99;

import me.chriss99.worldmanagement.Chunk;
import me.chriss99.worldmanagement.InfiniteChunkWorld;
import me.chriss99.worldmanagement.InfiniteWorld;
import me.chriss99.worldmanagement.MipMappedInfiniteChunkWorld;
import me.chriss99.worldmanagement.quadtree.IterationSurface;
import me.chriss99.worldmanagement.quadtree.IterationSurfaceRegionFileManager;
import org.joml.Vector2i;

import static me.chriss99.Array2DBufferWrapper.Type;

import static org.lwjgl.opengl.GL11.*;

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

    public final InfiniteWorld<IterationSurface> iterationInfo;

    public ErosionDataStorage(String worldName, int chunkSize, int regionSize, int iterationChunkSize, int iterationRegionSize) {
        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
        this.iterationChunkSize = iterationChunkSize;
        this.iterationRegionSize = iterationRegionSize;
        terrainGenerator = new TerrainGenerator(chunkSize);

        mipMappedTerrain = new MipMappedInfiniteChunkWorld(worldName + "/terrain", chunkSize, regionSize, terrainGenerator::generateChunk);
        mipMappedWater = new MipMappedInfiniteChunkWorld(worldName + "/water", chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));

        terrain = mipMappedTerrain.getMipMapLevel(0);
        water = mipMappedWater.getMipMapLevel(0);
        sediment = new InfiniteChunkWorld(worldName + "/sediment", Type.FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        hardness = new InfiniteChunkWorld(worldName + "/hardness", Type.FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1, 1)));

        waterOutflow = new InfiniteChunkWorld(worldName + "/waterOutflow", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        sedimentOutflow = new InfiniteChunkWorld(worldName + "/sedimentOutflow", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        thermalOutflow1 = new InfiniteChunkWorld(worldName + "/thermalOutflow1", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        thermalOutflow2 = new InfiniteChunkWorld(worldName + "/thermalOutflow2", Type.VEC4F, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        iterationInfo = new InfiniteWorld<>(iterationChunkSize, iterationRegionSize, (srcPos, size) -> new IterationSurface(new Vector2i(srcPos).mul(iterationChunkSize), size), new IterationSurfaceRegionFileManager(worldName + "/iteration", iterationChunkSize));
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
