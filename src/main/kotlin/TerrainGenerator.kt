package me.chriss99

import me.chriss99.program.ComputeProgram
import me.chriss99.worldmanagement.Chunk
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_RED
import org.lwjgl.opengl.GL15.GL_WRITE_ONLY
import org.lwjgl.opengl.GL20.glUniform2i
import org.lwjgl.opengl.GL30.GL_R32F
import org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT
import org.lwjgl.opengl.GL42.glMemoryBarrier
import org.lwjgl.opengl.GL43.glDispatchCompute

class TerrainGenerator(chunkSize: Int) : ComputeProgram("genHeightMap") {
    val terrainMap: Texture2D = Texture2D(GL_R32F, Vec2i(chunkSize))
    val srcPosUniform: Int = getUniform("srcPos")

    init {
        terrainMap.bindUniformImage(program, 8, "terrainMap", GL_WRITE_ONLY)
    }

    fun generateChunk(chunkPos: Vec2i, chunkSize: Int): Chunk {
        use()
        glUniform2i(srcPosUniform, chunkPos.x * chunkSize, chunkPos.y * chunkSize)

        glDispatchCompute(chunkSize, chunkSize, 1)
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)


        val buffer = Float2DBufferWrapper(Vec2i(chunkSize))
        terrainMap.downloadFullData(GL_RED, GL_FLOAT, buffer.buffer)

        return Chunk(buffer)
    }
}