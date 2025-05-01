package me.chriss99

import me.chriss99.worldmanagement.TileLoadManager
import org.joml.Vector2i

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

    override fun loadPolicy(tilePos: Vector2i, tile: T): Boolean {
        return true
    }

    override fun loadCommander(): Collection<Vector2i> {
        return listOf<Vector2i>()
    }
}