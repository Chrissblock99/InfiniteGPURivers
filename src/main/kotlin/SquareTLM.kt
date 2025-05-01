package me.chriss99

import glm_.func.common.abs
import me.chriss99.worldmanagement.TileLoadManager
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

open class SquareTLM<T>(var renderDistance: Int, initPos: Vec2) : TileLoadManager<T> {
    var position: Vec2 = Vec2(initPos)
        set(value) {
        field = Vec2(value)
    }

    override fun loadPolicy(tilePos: Vec2i, tile: T): Boolean {
        val distance: Vec2 = Vec2(tilePos).minus(position)
        return distance.x.abs < renderDistance && distance.y.abs < renderDistance
    }

    override fun loadCommander(): Collection<Vec2i> {
        val toLoad: ArrayList<Vec2i> = ArrayList<Vec2i>()

        for (x in -renderDistance..<renderDistance + 1)
            for (y in -renderDistance..<renderDistance + 1)
                toLoad.add(Vec2i(position.x.toInt() + x, position.y.toInt() + y))

        return toLoad
    }
}