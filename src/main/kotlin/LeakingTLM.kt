package me.chriss99

import me.chriss99.worldmanagement.TileLoadManager
import glm_.vec2.Vec2i

class LeakingTLM<T> : TileLoadManager<T> {
    init {
        System.err.println(
            """
                -------------------------------------
                
                This is (probably) leaking memory!
                If you see this someone was too lazy
                to implement proper load management
                of some tile based system.
                Restarting often enough might
                prevent crashes.
                
                -------------------------------------
                
                """.trimIndent()
        )
    }

    override fun loadPolicy(tilePos: Vec2i, tile: T): Boolean {
        return true
    }

    override fun loadCommander(): Collection<Vec2i> {
        return listOf<Vec2i>()
    }
}