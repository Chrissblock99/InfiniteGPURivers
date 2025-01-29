package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.function.Function;

/**
 * Extends TileMap2Ds functionality by directly integrating fileStorage
 *
 * @param <T> Type of the Tiles to be managed
 */
public class FileBackedTileMap2D<T> extends TileMap2D<T> {
    protected final FileStorage<Vector2i, T> fileStorage;

    public FileBackedTileMap2D(Function<Vector2i, T> tileGenerator, FileStorage<Vector2i, T> fileStorage, TileLoadManager<T> loadManager) {
        super(v ->
                (fileStorage.hasFile(v)) ?
                    fileStorage.loadFile(v) :
                    tileGenerator.apply(v),
        fileStorage::saveFile, loadManager);
        this.fileStorage = fileStorage;
    }
}
