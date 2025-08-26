package me.chriss99

import glm_.func.common.abs
import me.chriss99.worldmanagement.TileLoadManager
import glm_.vec2.Vec2i

open class SquareTLM<T>(var radius: Int, var center: Vec2i) : TileLoadManager<T> {
    override fun loadPolicy(tilePos: Vec2i, tile: T): Boolean {
        val distance = tilePos - center
        return distance.x.abs < radius && distance.y.abs < radius
    }

    override fun loadCommander(): Collection<Vec2i> {
        val toLoad: ArrayList<Vec2i> = ArrayList()

        for (x in -radius..radius)
            for (y in -radius..radius)
                toLoad.add(center + Vec2i(x, y))

        return toLoad
    }
}