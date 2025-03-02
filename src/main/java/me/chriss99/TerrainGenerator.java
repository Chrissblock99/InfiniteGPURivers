package me.chriss99;

import me.chriss99.program.ComputeProgram;
import me.chriss99.worldmanagement.Chunk;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL20.glUniform2i;
import static org.lwjgl.opengl.GL30.GL_R32F;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class TerrainGenerator extends ComputeProgram {
    final Texture2D terrainMap;
    final int srcPosUniform;

    public TerrainGenerator(int chunkSize) {
        super("genHeightMap");
        terrainMap = new Texture2D(GL_R32F, new Vector2i(chunkSize));
        srcPosUniform = getUniform("srcPos");

        terrainMap.bindUniformImage(program, 8, "terrainMap", GL_WRITE_ONLY);
    }

    public Chunk generateChunk(Vector2i chunkPos, int chunkSize) {
        use();
        glUniform2i(srcPosUniform, chunkPos.x*chunkSize, chunkPos.y*chunkSize);

        glDispatchCompute(chunkSize, chunkSize, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


        Float2DBufferWrapper buffer = new Float2DBufferWrapper(new Vector2i(chunkSize));
        terrainMap.downloadFullData(GL_RED, GL_FLOAT, buffer.buffer);

        return new Chunk(buffer);
    }
}
