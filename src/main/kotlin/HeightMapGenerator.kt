package me.chriss99

import me.chriss99.program.ComputeProgram
import me.chriss99.worldmanagement.Chunk
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL15.GL_WRITE_ONLY
import org.lwjgl.opengl.GL20.glUniform2i
import org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT
import org.lwjgl.opengl.GL42.glMemoryBarrier
import org.lwjgl.opengl.GL43.glDispatchCompute

class HeightMapGenerator(name: String, bindingUnit: Int, chunkSize: Int) : ComputeProgram(name) {
    val heightMap: Texture2D = Texture2D(Array2DBufferWrapper.Type.FLOAT, Vec2i(chunkSize))
    val srcPosUniform: Int = getUniform("srcPos")

    init {
        heightMap.bindUniformImage(program, bindingUnit, "heightMap", GL_WRITE_ONLY)
    }

    fun generateChunk(chunkPos: Vec2i, chunkSize: Int): Chunk {
        use()
        glUniform2i(srcPosUniform, chunkPos.x * chunkSize, chunkPos.y * chunkSize)

        glDispatchCompute(chunkSize, chunkSize, 1)
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)


        val buffer = Float2DBufferWrapper(Vec2i(chunkSize))
        heightMap.downloadFullData(buffer)

        return Chunk(buffer)
    }
}