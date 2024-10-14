package me.chriss99;

import me.chriss99.worldmanagement.Chunk;
import me.chriss99.worldmanagement.InfiniteWorld;

import static org.lwjgl.opengl.GL11.*;

public class ErosionDataStorage {
    private final TerrainGenerator terrainGenerator;

    public final InfiniteWorld terrain;
    public final InfiniteWorld water;
    public final InfiniteWorld sediment;
    public final InfiniteWorld hardness;

    public final InfiniteWorld waterOutflow;
    public final InfiniteWorld sedimentOutflow;

    public final InfiniteWorld thermalOutflow1;
    public final InfiniteWorld thermalOutflow2;

    public ErosionDataStorage(String worldName, int chunkSize, int regionSize) {
        terrainGenerator = new TerrainGenerator(chunkSize);

        terrain = new InfiniteWorld(worldName + "/terrain", GL_RED, GL_FLOAT, chunkSize, regionSize, terrainGenerator::generateChunk);
        water = new InfiniteWorld(worldName + "/water", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        sediment = new InfiniteWorld(worldName + "/sediment", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        hardness = new InfiniteWorld(worldName + "/hardness", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1, 1)));

        waterOutflow = new InfiniteWorld(worldName + "/waterOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        sedimentOutflow = new InfiniteWorld(worldName + "/sedimentOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        thermalOutflow1 = new InfiniteWorld(worldName + "/thermalOutflow1", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        thermalOutflow2 = new InfiniteWorld(worldName + "/thermalOutflow2", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
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
    }

    public void cleanGL() {
        terrainGenerator.delete();
    }
}
