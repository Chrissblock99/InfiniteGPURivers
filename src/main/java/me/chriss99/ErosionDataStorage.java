package me.chriss99;

import me.chriss99.worldmanagement.Chunk;
import me.chriss99.worldmanagement.InfiniteChunkWorld;
import me.chriss99.worldmanagement.InfiniteWorld;
import me.chriss99.worldmanagement.quadtree.IterationSurface;
import me.chriss99.worldmanagement.quadtree.IterationSurfaceRegionFileManager;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL11.*;

public class ErosionDataStorage {
    public final int chunkSize;
    public final int regionSize;
    public final int iterationChunkSize;
    public final int iterationRegionSize;

    private final TerrainGenerator terrainGenerator;

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

        terrain = new InfiniteChunkWorld(worldName + "/terrain", GL_RED, GL_FLOAT, chunkSize, regionSize, terrainGenerator::generateChunk);
        water = new InfiniteChunkWorld(worldName + "/water", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        sediment = new InfiniteChunkWorld(worldName + "/sediment", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        hardness = new InfiniteChunkWorld(worldName + "/hardness", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1, 1)));

        waterOutflow = new InfiniteChunkWorld(worldName + "/waterOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        sedimentOutflow = new InfiniteChunkWorld(worldName + "/sedimentOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        thermalOutflow1 = new InfiniteChunkWorld(worldName + "/thermalOutflow1", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        thermalOutflow2 = new InfiniteChunkWorld(worldName + "/thermalOutflow2", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        iterationInfo = new InfiniteWorld<>(iterationChunkSize, iterationRegionSize, (srcPos, size) -> new IterationSurface(new Vector2i(srcPos).mul(iterationChunkSize), size), new IterationSurfaceRegionFileManager(worldName + "/iteration", iterationChunkSize));
    }

    public void unloadAll() {
        terrain.unloadAllRegions();
        water.unloadAllRegions();
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
