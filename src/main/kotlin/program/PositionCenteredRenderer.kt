package me.chriss99.program

import me.chriss99.Area
import me.chriss99.render.ChunkVAO
import me.chriss99.CutOutRectangleTLM
import me.chriss99.worldmanagement.TileMap2D
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import me.chriss99.util.Util.floorDiv

open class PositionCenteredRenderer<T : ChunkVAO>(
    renderProgram: RenderProgram<T>,
    chunkLoader: (pos: Vec2i, chunkSize: Int) -> T,
    position: Vec3,
    chunkSize: Int,
    chunkRenderDistance: Int,
    skipArea: Area
) {
    protected val renderProgram: RenderProgram<T> = renderProgram
    protected val chunkVaos: TileMap2D<T>
    protected val loadManager = CutOutRectangleTLM<T>(chunkRenderDistance, Vec2i(position.xz), skipArea)

    val chunkSize: Int
    var chunkRenderDistance: Int
        get() = loadManager.radius
        set(value) {
            loadManager.radius = value
            chunkVaos.manageLoad()
        }

    constructor(
        renderProgram: RenderProgram<T>,
        chunkLoader: (pos: Vec2i, chunkSize: Int) -> T,
        position: Vec3,
        chunkSize: Int,
        chunkRenderDistance: Int
    ) : this(renderProgram, chunkLoader, position, chunkSize, chunkRenderDistance, Area())

    init {
        this.chunkVaos = TileMap2D(
            { pos -> chunkLoader(pos * chunkSize, chunkSize) },
            { _, t -> t.delete() }, loadManager
        )
        this.chunkSize = chunkSize

        updateLoadedChunks(position, skipArea)
    }

    fun updateLoadedChunks(newPosition: Vec3) = updateLoadedChunks(newPosition, null)

    fun updateLoadedChunks(newPosition: Vec3, skipArea: Area?) {
        loadManager.center = (Vec2i(newPosition.xz) floorDiv chunkSize)
        if (skipArea != null)
            loadManager.skipArea = skipArea / chunkSize
        chunkVaos.manageLoad()
    }

    fun reloadAll() = chunkVaos.reloadAll()
    fun render() = renderProgram.render(chunkVaos.allTiles)

    fun delete() {
        chunkVaos.unloadAll()
        renderProgram.delete()
    }
}