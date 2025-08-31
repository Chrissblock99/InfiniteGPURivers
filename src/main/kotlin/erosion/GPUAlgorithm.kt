package me.chriss99.erosion

import glm_.vec2.Vec2i
import me.chriss99.Array2DBufferWrapper
import me.chriss99.Byte2DBufferWrapper
import me.chriss99.Float2DBufferWrapper
import me.chriss99.NothingTLM
import me.chriss99.Texture2D
import me.chriss99.Vec4f2DBufferWrapper
import me.chriss99.program.ComputeProgram
import me.chriss99.worldmanagement.Chunk
import me.chriss99.worldmanagement.InfiniteChunkWorld
import me.chriss99.worldmanagement.Region
import me.chriss99.worldmanagement.TileLoadManager
import me.chriss99.worldmanagement.iteration.IterableWorld
import org.lwjgl.opengl.GL15
import java.util.LinkedList

abstract class GPUAlgorithm(val worldName: String, val maxTextureSize: Vec2i) {
    val chunkSize = 64
    val regionSize = 10
    val iterationChunkSize = 64
    val iterationRegionSize = 10


    val resources = LinkedList<Resource>()
    val iteration = IterableWorld("$worldName/iteration", iterationChunkSize, iterationRegionSize,
        NothingTLM())

    val computationStages = LinkedList<ComputationStage>()


    val nothingLoadManager = NothingTLM<Region<Chunk>>()


    fun manageLoad(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i) {
        updateLoadManagers(chunkRenderDistance, chunkLoadBufferDistance, playerPos)

        resources.forEach { it.manageLoad() }
        iteration.manageLoad()
    }

    abstract fun updateLoadManagers(chunkRenderDistance: Int, chunkLoadBufferDistance: Int, playerPos: Vec2i)

    fun unloadAll() {
        resources.forEach { it.unloadAllRegions() }
        iteration.unloadAllRegions()
    }

    fun delete() {
        resources.forEach { it.texture.delete() }
        computationStages.forEach { it.computeProgram.delete() }
        cleanGL()
    }

    protected abstract fun cleanGL()




    enum class Access(val glInt: Int) {
        READ_ONLY(GL15.GL_READ_ONLY),
        WRITE_ONLY(GL15.GL_WRITE_ONLY),
        READ_WRITE(GL15.GL_READ_WRITE)
    }

    inner class Resource(val name: String, type: Array2DBufferWrapper.Type,
                         tileLoadManager: TileLoadManager<Region<Chunk>> = nothingLoadManager,
                         chunkGenerator: (pos: Vec2i, size: Int) -> Chunk = bufferOfTypeConstructor(type)) {
        val world = InfiniteChunkWorld("$worldName/$name", type, chunkSize, regionSize, chunkGenerator, tileLoadManager)
        val texture: Texture2D = Texture2D(type.glInternalFormat, maxTextureSize+2) //the plus 2 is a read buffer in all directions (avoids implicit out of bound reads when iterating near edges)
        init { resources.add(this) }

        fun manageLoad() = world.manageLoad()
        fun unloadAllRegions() = world.unloadAllRegions()
    }

    inner class ComputationStage(shaderName: String, access: Map<Resource, Access>) {
        val computeProgram = ComputeProgram(shaderName)
        val srcPosUniform = computeProgram.getUniform("srcPos")

        init {
            access.forEach { (resource, value) -> resource.texture.bindUniformImage(computeProgram.program, resources.indexOf(resource), "${resource.name}Map", value.glInt) }
            computationStages.add(this)
        }
    }

    companion object {
        fun bufferOfTypeConstructor(type: Array2DBufferWrapper.Type): (pos: Vec2i, size: Int) -> Chunk = when (type) {
            Array2DBufferWrapper.Type.BYTE -> { _: Vec2i, chunkSize: Int -> Chunk(Byte2DBufferWrapper(Vec2i(chunkSize))) }
            Array2DBufferWrapper.Type.FLOAT -> { _: Vec2i, chunkSize: Int -> Chunk(Float2DBufferWrapper(Vec2i(chunkSize))) }
            Array2DBufferWrapper.Type.VEC4F -> { _: Vec2i, chunkSize: Int -> Chunk(Vec4f2DBufferWrapper(Vec2i(chunkSize))) }
        }
    }
}