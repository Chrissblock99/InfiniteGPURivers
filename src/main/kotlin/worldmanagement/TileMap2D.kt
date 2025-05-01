package me.chriss99.worldmanagement

import org.joml.Vector2i
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * Represents a map of Tiles, guarantees that ANY Vector2i can be asked for and stores previously loaded Tiles <br></br>
 * loading and unloading behaviour can be augmented with the TileLoadManager
 *
 * @param <T> Type of the Tiles to be managed
</T> */
open class TileMap2D<T>(
    tileLoader: Function<Vector2i, T>,
    tileUnloader: BiConsumer<Vector2i, T>,
    loadManager: TileLoadManager<T>
) {
    protected val loadedTiles: HashMap<Vector2i, T> = LinkedHashMap<Vector2i, T>()
    protected val tileLoader: Function<Vector2i, T>
    protected val tileUnloader: BiConsumer<Vector2i, T>
    protected var loadManager: TileLoadManager<T>

    init {
        this.tileLoader = tileLoader
        this.tileUnloader = tileUnloader
        this.loadManager = loadManager
    }

    fun getTile(coord: Vector2i): T {
        return loadedTiles.computeIfAbsent(coord, tileLoader)
    }

    val allTiles: Collection<T>
        get() = loadedTiles.values.toList()

    fun reloadAll() {
        loadedTiles.replaceAll { k: Vector2i, v: T ->
            tileUnloader.accept(k, v)
            tileLoader.apply(k)
        }
    }

    fun manageLoad() {
        val toRemove: HashSet<Vector2i> = HashSet<Vector2i>()

        loadedTiles.forEach { (v: Vector2i, t: T) ->
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