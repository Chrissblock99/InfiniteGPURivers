package me.chriss99.worldmanagement

import org.joml.Vector2i
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * Extends TileMap2Ds functionality by directly integrating fileStorage
 *
 * @param <T> Type of the Tiles to be managed
</T> */
class FileBackedTileMap2D<T>(
    tileGenerator: Function<Vector2i, T>,
    protected val fileStorage: FileStorage<Vector2i, T>,
    loadManager: TileLoadManager<T>
) :
    TileMap2D<T>(
        Function { v: Vector2i -> if (fileStorage.hasFile(v)) fileStorage.loadFile(v) else tileGenerator.apply(v) },
        BiConsumer { key: Vector2i, file: T -> fileStorage.saveFile(key, file) }, loadManager
    )