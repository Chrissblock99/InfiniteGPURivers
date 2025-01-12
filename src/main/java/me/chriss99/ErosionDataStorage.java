package me.chriss99;

import me.chriss99.worldmanagement.Chunk;
import me.chriss99.worldmanagement.InfiniteChunkWorld;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class ErosionDataStorage {
    public final int chunkSize;
    public final int regionSize;

    private final TerrainGenerator terrainGenerator;

    public final InfiniteChunkWorld terrain;
    public final InfiniteChunkWorld water;
    public final InfiniteChunkWorld sediment;
    public final InfiniteChunkWorld hardness;

    public final InfiniteChunkWorld waterOutflow;
    public final InfiniteChunkWorld sedimentOutflow;

    public final InfiniteChunkWorld thermalOutflow1;
    public final InfiniteChunkWorld thermalOutflow2;

    private final InfiniteChunkWorld iterationInfo;

    public ErosionDataStorage(String worldName, int chunkSize, int regionSize) {
        this.chunkSize = chunkSize;
        this.regionSize = regionSize;
        terrainGenerator = new TerrainGenerator(chunkSize);

        terrain = new InfiniteChunkWorld(worldName + "/terrain", GL_RED, GL_FLOAT, chunkSize, regionSize, terrainGenerator::generateChunk);
        water = new InfiniteChunkWorld(worldName + "/water", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        sediment = new InfiniteChunkWorld(worldName + "/sediment", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        hardness = new InfiniteChunkWorld(worldName + "/hardness", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1, 1)));

        waterOutflow = new InfiniteChunkWorld(worldName + "/waterOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        sedimentOutflow = new InfiniteChunkWorld(worldName + "/sedimentOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        thermalOutflow1 = new InfiniteChunkWorld(worldName + "/thermalOutflow1", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        thermalOutflow2 = new InfiniteChunkWorld(worldName + "/thermalOutflow2", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        iterationInfo = new InfiniteChunkWorld(worldName + "/iteration", GL_RED, GL_INT, regionSize, 100, (vector2i, chunkSize1) -> new Chunk(new Array2DBufferWrapper(GL_RED, GL_INT, chunkSize1, chunkSize1)));
    }

    public int iterationOf(Vector2i chunkCoord) {
        int data = iterationInfo.readArea(chunkCoord.x, chunkCoord.y, 1, 1).buffer.getInt();
        return data & 0x0FFFFFFF;
    }

    private byte iterationSurfaceTypeBitsOf(Vector2i chunkCoord) {
        int data = iterationInfo.readArea(chunkCoord.x, chunkCoord.y, 1, 1).buffer.getInt();
        return (byte) ((data & 0xF0000000) >>> 28);
    }

    public IterationSurfaceType iterationSurfaceTypeOf(Vector2i chunkCoord) {
        return new IterationSurfaceType(iterationSurfaceTypeBitsOf(chunkCoord));
    }

    public void setIterationOf(Vector2i chunkCoord, int iteration) {
        if ((iteration & 0xF0000000) != 0)
            throw new IllegalArgumentException("Iteration too large: " + iteration + ". Maximum is " + 0x0FFFFFFF);

        int data = (((int) iterationSurfaceTypeBitsOf(chunkCoord)) << 28) | (iteration & 0x0FFFFFFF);

        iterationInfo.writeArea(chunkCoord.x, chunkCoord.y, new Array2DBufferWrapper(BufferUtils.createByteBuffer(4).putInt(data), GL_RED, GL_INT, 1, 1));
    }

    public void setIterationSurfaceTypeOf(Vector2i chunkCoord, IterationSurfaceType surfaceType) {
        int data = (((int) surfaceType.toBits()) << 28) | iterationOf(chunkCoord);

        iterationInfo.writeArea(chunkCoord.x, chunkCoord.y, new Array2DBufferWrapper(BufferUtils.createByteBuffer(4).putInt(data), GL_RED, GL_INT, 1, 1));
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
