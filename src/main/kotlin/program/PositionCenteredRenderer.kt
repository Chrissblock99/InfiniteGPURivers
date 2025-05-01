package me.chriss99.program

import me.chriss99.Area
import me.chriss99.ChunkVAO
import me.chriss99.CutOutRectangleTLM
import me.chriss99.worldmanagement.TileMap2D
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import java.util.function.BiFunction

class PositionCenteredRenderer<T : ChunkVAO>(
    renderProgram: RenderProgram<T>,
    chunkLoader: BiFunction<Vector2i, Int, T>,
    position: Vector3f,
    chunkSize: Int,
    chunkRenderDistance: Int,
    skipArea: Area
) {
    protected val renderProgram: RenderProgram<T> = renderProgram
    protected val chunkVaos: TileMap2D<T>
    protected val loadManager: CutOutRectangleTLM<T> =
        CutOutRectangleTLM<T>(chunkRenderDistance, Vector2f(position.x, position.z), skipArea)

    val chunkSize: Int
    var chunkRenderDistance: Int
        get() = loadManager.renderDistance
        set(value) {
            loadManager.renderDistance = value
            chunkVaos.manageLoad()
        }

    constructor(
        renderProgram: RenderProgram<T>,
        chunkLoader: BiFunction<Vector2i, Int, T>,
        position: Vector3f,
        chunkSize: Int,
        chunkRenderDistance: Int
    ) : this(renderProgram, chunkLoader, position, chunkSize, chunkRenderDistance, Area())

    init {
        this.chunkVaos = TileMap2D(
            { key -> chunkLoader.apply(Vector2i(key).mul(chunkSize), chunkSize) },
            { v, t -> t.delete() },
            loadManager
        )
        this.chunkSize = chunkSize

        updateLoadedChunks(position, skipArea)
    }

    fun updateLoadedChunks(newPosition: Vector3f) {
        updateLoadedChunks(newPosition, null)
    }

    fun updateLoadedChunks(newPosition: Vector3f, skipArea: Area?) {
        loadManager.position = Vector2f(newPosition.x, newPosition.z).div(chunkSize.toFloat()).floor()
        if (skipArea != null) loadManager.skipArea = skipArea.div(chunkSize)
        chunkVaos.manageLoad()
    }

    fun reloadAll() {
        chunkVaos.reloadAll()
    }

    fun render() {
        renderProgram.render(chunkVaos.allTiles)
    }

    fun delete() {
        chunkVaos.unloadAll()
        renderProgram.delete()
    }
}