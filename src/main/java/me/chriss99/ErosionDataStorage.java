package me.chriss99;

import me.chriss99.worldmanagement.Chunk;
import me.chriss99.worldmanagement.InfiniteWorld;
import org.joml.Vector2i;

import java.util.function.Function;

import static org.lwjgl.opengl.GL11.*;

public class ErosionDataStorage {
    public final InfiniteWorld terrain;
    public final InfiniteWorld water;
    public final InfiniteWorld sediment;
    public final InfiniteWorld hardness;

    public final InfiniteWorld waterOutflow;
    public final InfiniteWorld sedimentOutflow;

    public final InfiniteWorld thermalOutflow1;
    public final InfiniteWorld thermalOutflow2;

    public ErosionDataStorage(String worldName, Function<Vector2i, Chunk> chunkGenerator) {
        terrain = new InfiniteWorld(worldName + "/terrain", GL_RED, GL_FLOAT, chunkGenerator);
        water = new InfiniteWorld(worldName + "/water", GL_RED, GL_FLOAT, vector2i -> new Chunk(new Float2DBufferWrapper(100, 100)));
        sediment = new InfiniteWorld(worldName + "/sediment", GL_RED, GL_FLOAT, vector2i -> new Chunk(new Float2DBufferWrapper(100, 100)));
        hardness = new InfiniteWorld(worldName + "/water", GL_RED, GL_FLOAT, vector2i -> {
            Float2DBufferWrapper wrapper = new Float2DBufferWrapper(100, 100);
            for (int i = 0; i < 100 * 100; i++)
                wrapper.buffer.putFloat(1);
            return new Chunk(wrapper);
        });

        waterOutflow = new InfiniteWorld(worldName + "/waterOutflow", GL_RGBA, GL_FLOAT, vector2i -> new Chunk(new Vec4f2DBufferWrapper(100, 100)));
        sedimentOutflow = new InfiniteWorld(worldName + "/sedimentOutflow", GL_RGBA, GL_FLOAT, vector2i -> new Chunk(new Vec4f2DBufferWrapper(100, 100)));

        thermalOutflow1 = new InfiniteWorld(worldName + "/thermalOutflow1", GL_RGBA, GL_FLOAT, vector2i -> new Chunk(new Vec4f2DBufferWrapper(100, 100)));
        thermalOutflow2 = new InfiniteWorld(worldName + "/thermalOutflow2", GL_RGBA, GL_FLOAT, vector2i -> new Chunk(new Vec4f2DBufferWrapper(100, 100)));
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
}
