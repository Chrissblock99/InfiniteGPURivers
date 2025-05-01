package me.chriss99

import me.chriss99.worldmanagement.TileLoadManager
import org.joml.Vector2f
import org.joml.Vector2i

open class SquareTLM<T>(var renderDistance: Int, initPos: Vector2f) : TileLoadManager<T> {
    var position: Vector2f = Vector2f(initPos)
        set(value) {
        field = Vector2f(value)
    }

    override fun loadPolicy(tilePos: Vector2i, tile: T): Boolean {
        val distance: Vector2f = Vector2f(tilePos).sub(position).absolute()
        return distance.x < renderDistance && distance.y < renderDistance
    }

    override fun loadCommander(): Collection<Vector2i> {
        val toLoad: ArrayList<Vector2i> = ArrayList<Vector2i>()

        for (x in -renderDistance..<renderDistance + 1) for (y in -renderDistance..<renderDistance + 1) toLoad.add(
            Vector2i(position.x.toInt() + x, position.y.toInt() + y)
        )

        return toLoad
    }
}