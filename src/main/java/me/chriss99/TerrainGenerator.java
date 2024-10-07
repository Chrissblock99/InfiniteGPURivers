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

public class TerrainGenerator {
    public static Chunk generateChunk(Vector2i chunkPos) {
        Texture2D terrainMap = new Texture2D(GL_R32F, 100, 100);
        ComputeProgram genHeightMap = new ComputeProgram("genHeightMap");
        int srcPosUniform = genHeightMap.getUniform("srcPos");

        terrainMap.bindUniformImage(genHeightMap.program, 8, "terrainMap", GL_WRITE_ONLY);
        glUniform2i(srcPosUniform, chunkPos.x*100, chunkPos.y*100);

        genHeightMap.use();
        glDispatchCompute(100, 100, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


        Float2DBufferWrapper buffer = new Float2DBufferWrapper(100, 100);
        terrainMap.downloadFullData(GL_RED, GL_FLOAT, buffer.buffer);

        return new Chunk(buffer);
    }
}
