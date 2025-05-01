package me.chriss99

import org.joml.Vector2f
import org.joml.Vector2i

class CutOutRectangleTLM<T>(renderDistance: Int, initPos: Vector2f, skipArea: Area) : SquareTLM<T>(renderDistance, initPos) {
    var skipArea: Area

    init {
        this.skipArea = skipArea.copy()
    }

    override fun loadPolicy(tilePos: Vector2i, tile: T): Boolean {
        return super.loadPolicy(tilePos, tile) && !skipArea.contains(tilePos)
    }

    override fun loadCommander(): Collection<Vector2i> {
        val toLoad: MutableList<Vector2i> = mutableListOf(*super.loadCommander().toTypedArray())
        toLoad.removeIf { v: Vector2i -> skipArea.contains(v) }
        return toLoad
    }
}