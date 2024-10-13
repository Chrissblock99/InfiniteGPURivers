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

    public ErosionDataStorage(String worldName) {
        terrainGenerator = new TerrainGenerator(100);

        terrain = new InfiniteWorld(worldName + "/terrain", GL_RED, GL_FLOAT, 100, terrainGenerator::generateChunk);
        water = new InfiniteWorld(worldName + "/water", GL_RED, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Float2DBufferWrapper(chunkSize, chunkSize)));
        sediment = new InfiniteWorld(worldName + "/sediment", GL_RED, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Float2DBufferWrapper(chunkSize, chunkSize)));
        hardness = new InfiniteWorld(worldName + "/hardness", GL_RED, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Float2DBufferWrapper(chunkSize, chunkSize, 1)));

        waterOutflow = new InfiniteWorld(worldName + "/waterOutflow", GL_RGBA, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize, chunkSize)));
        sedimentOutflow = new InfiniteWorld(worldName + "/sedimentOutflow", GL_RGBA, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize, chunkSize)));

        thermalOutflow1 = new InfiniteWorld(worldName + "/thermalOutflow1", GL_RGBA, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize, chunkSize)));
        thermalOutflow2 = new InfiniteWorld(worldName + "/thermalOutflow2", GL_RGBA, GL_FLOAT, 100, (vector2i, chunkSize) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize, chunkSize)));
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
