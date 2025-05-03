package me.chriss99.worldmanagement

import glm_.vec2.Vec2i

/**
 * Represents a map of Tiles, guarantees that ANY Vec2i can be asked for and stores previously loaded Tiles <br></br>
 * loading and unloading behaviour can be augmented with the TileLoadManager
 *
 * @param <T> Type of the Tiles to be managed
</T> */
open class TileMap2D<T>(
    private val loadTile: (pos: Vec2i) -> T,
    private val unloadTile: (pos: Vec2i, tile: T) -> Unit,
    private var loadManager: TileLoadManager<T>
) {
    private val loadedTiles: HashMap<Vec2i, T> = LinkedHashMap<Vec2i, T>()
    val allTiles get() = loadedTiles.values.toList()

    operator fun get(coord: Vec2i): T = loadedTiles.computeIfAbsent(coord, loadTile)

    fun reloadAll() {
        loadedTiles.replaceAll { k: Vec2i, v: T ->
            unloadTile(k, v)
            loadTile(k)
        }
    }

    fun manageLoad() {
        val toRemove = HashSet<Vec2i>()

        loadedTiles.forEach { (v: Vec2i, t: T) ->
            if (!loadManager.loadPolicy(v, t))
                toRemove.add(v)
        }

        for (v in loadManager.loadCommander())
            if (!toRemove.remove(v))
                get(v)

        for (v in toRemove)
            unloadTile(v, loadedTiles.remove(v)!!)
    }

    fun unloadAll() {
        loadedTiles.forEach(unloadTile)
        loadedTiles.clear()
    }
}