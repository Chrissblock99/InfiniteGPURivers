package me.chriss99.worldmanagement

import glm_.vec2.Vec2i
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * Represents a map of Tiles, guarantees that ANY Vec2i can be asked for and stores previously loaded Tiles <br></br>
 * loading and unloading behaviour can be augmented with the TileLoadManager
 *
 * @param <T> Type of the Tiles to be managed
</T> */
open class TileMap2D<T>(
    tileLoader: Function<Vec2i, T>,
    tileUnloader: BiConsumer<Vec2i, T>,
    loadManager: TileLoadManager<T>
) {
    protected val loadedTiles: HashMap<Vec2i, T> = LinkedHashMap<Vec2i, T>()
    protected val tileLoader: Function<Vec2i, T>
    protected val tileUnloader: BiConsumer<Vec2i, T>
    protected var loadManager: TileLoadManager<T>

    init {
        this.tileLoader = tileLoader
        this.tileUnloader = tileUnloader
        this.loadManager = loadManager
    }

    fun getTile(coord: Vec2i): T {
        return loadedTiles.computeIfAbsent(coord, tileLoader)
    }

    val allTiles: Collection<T>
        get() = loadedTiles.values.toList()

    fun reloadAll() {
        loadedTiles.replaceAll { k: Vec2i, v: T ->
            tileUnloader.accept(k, v)
            tileLoader.apply(k)
        }
    }

    fun manageLoad() {
        val toRemove: HashSet<Vec2i> = HashSet<Vec2i>()

        loadedTiles.forEach { (v: Vec2i, t: T) ->
            if (!loadManager.loadPolicy(v, t))
                toRemove.add(v)
        }

        for (v in loadManager.loadCommander())
            if (!toRemove.remove(v))
                getTile(v)

        for (v in toRemove)
            tileUnloader.accept(v, loadedTiles.remove(v)!!)
    }

    fun unloadAll() {
        loadedTiles.forEach(tileUnloader)
        loadedTiles.clear()
    }
}