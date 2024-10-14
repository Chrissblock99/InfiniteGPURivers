package me.chriss99;

import me.chriss99.worldmanagement.Chunk;
import me.chriss99.worldmanagement.InfiniteWorld;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class ErosionDataStorage {
    public final int regionSize;

    private final TerrainGenerator terrainGenerator;

    public final InfiniteWorld terrain;
    public final InfiniteWorld water;
    public final InfiniteWorld sediment;
    public final InfiniteWorld hardness;

    public final InfiniteWorld waterOutflow;
    public final InfiniteWorld sedimentOutflow;

    public final InfiniteWorld thermalOutflow1;
    public final InfiniteWorld thermalOutflow2;

    private final InfiniteWorld iterationInfo;

    public ErosionDataStorage(String worldName, int chunkSize, int regionSize) {
        this.regionSize = regionSize;
        terrainGenerator = new TerrainGenerator(chunkSize);

        terrain = new InfiniteWorld(worldName + "/terrain", GL_RED, GL_FLOAT, chunkSize, regionSize, terrainGenerator::generateChunk);
        water = new InfiniteWorld(worldName + "/water", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        sediment = new InfiniteWorld(worldName + "/sediment", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1)));
        hardness = new InfiniteWorld(worldName + "/hardness", GL_RED, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Float2DBufferWrapper(chunkSize1, chunkSize1, 1)));

        waterOutflow = new InfiniteWorld(worldName + "/waterOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        sedimentOutflow = new InfiniteWorld(worldName + "/sedimentOutflow", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        thermalOutflow1 = new InfiniteWorld(worldName + "/thermalOutflow1", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));
        thermalOutflow2 = new InfiniteWorld(worldName + "/thermalOutflow2", GL_RGBA, GL_FLOAT, chunkSize, regionSize, (vector2i, chunkSize1) -> new Chunk(new Vec4f2DBufferWrapper(chunkSize1, chunkSize1)));

        iterationInfo = new InfiniteWorld(worldName + "/iteration", GL_RED, GL_INT, regionSize, 100, (vector2i, chunkSize1) -> new Chunk(new Array2DBufferWrapper(GL_RED, GL_INT, chunkSize1, chunkSize1)));
    }

    public int iterationOf(Vector2i chunkCoord) {
        int data = iterationInfo.readArea(Util.properIntDivide(chunkCoord.x, regionSize), Util.properIntDivide(chunkCoord.y, regionSize), 1, 1).buffer.getInt();
        return data & 0x0FFFFFFF;
    }

    public byte iterationSurfaceTypeOf(Vector2i chunkCoord) {
        int data = iterationInfo.readArea(Util.properIntDivide(chunkCoord.x, regionSize), Util.properIntDivide(chunkCoord.y, regionSize), 1, 1).buffer.getInt();
        return (byte) ((data & 0xF0000000) >>> 28);
    }

    public void setIterationOf(Vector2i chunkCoord, int iteration) {
        int x = Util.properIntDivide(chunkCoord.x, regionSize);
        int y = Util.properIntDivide(chunkCoord.y, regionSize);

        int data = iterationSurfaceTypeOf(new Vector2i(x, y)) | (iteration & 0x0FFFFFFF);

        iterationInfo.writeArea(x, y, new Array2DBufferWrapper(BufferUtils.createByteBuffer(4).putInt(data), GL_RED, GL_INT, 1, 1));
    }

    public void setIterationSurfaceTypeOf(Vector2i chunkCoord, byte surfaceType) {
        int x = Util.properIntDivide(chunkCoord.x, regionSize);
        int y = Util.properIntDivide(chunkCoord.y, regionSize);

        int data = (((int) surfaceType) << 28) | iterationOf(new Vector2i(x, y));

        iterationInfo.writeArea(x, y, new Array2DBufferWrapper(BufferUtils.createByteBuffer(4).putInt(data), GL_RED, GL_INT, 1, 1));
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
