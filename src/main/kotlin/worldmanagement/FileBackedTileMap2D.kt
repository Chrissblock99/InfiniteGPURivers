package me.chriss99.worldmanagement

import glm_.vec2.Vec2i

/**
 * Extends TileMap2Ds functionality by directly integrating fileStorage
 *
 * @param <T> Type of the Tiles to be managed
</T> */
class FileBackedTileMap2D<T>(
    generateTile: (pos: Vec2i) -> T,
    fileStorage: FileStorage<Vec2i, T>,
    loadManager: TileLoadManager<T>
) :
    TileMap2D<T>(
        { if (fileStorage.hasFile(it)) fileStorage.loadFile(it) else generateTile(it) },
        fileStorage::saveFile, loadManager
    )