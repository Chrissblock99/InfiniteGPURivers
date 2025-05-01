package me.chriss99.worldmanagement

import glm_.vec2.Vec2i
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * Extends TileMap2Ds functionality by directly integrating fileStorage
 *
 * @param <T> Type of the Tiles to be managed
</T> */
class FileBackedTileMap2D<T>(
    tileGenerator: Function<Vec2i, T>,
    protected val fileStorage: FileStorage<Vec2i, T>,
    loadManager: TileLoadManager<T>
) :
    TileMap2D<T>(
        Function { v: Vec2i -> if (fileStorage.hasFile(v)) fileStorage.loadFile(v) else tileGenerator.apply(v) },
        BiConsumer { key: Vec2i, file: T -> fileStorage.saveFile(key, file) }, loadManager
    )