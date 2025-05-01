package me.chriss99

import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class CutOutRectangleTLM<T>(renderDistance: Int, initPos: Vec2, skipArea: Area) : SquareTLM<T>(renderDistance, initPos) {
    var skipArea: Area

    init {
        this.skipArea = skipArea.copy()
    }

    override fun loadPolicy(tilePos: Vec2i, tile: T): Boolean {
        return super.loadPolicy(tilePos, tile) && !skipArea.contains(tilePos)
    }

    override fun loadCommander(): Collection<Vec2i> {
        val toLoad: MutableList<Vec2i> = mutableListOf(*super.loadCommander().toTypedArray())
        toLoad.removeIf { v: Vec2i -> skipArea.contains(v) }
        return toLoad
    }
}