package me.chriss99.worldmanagement;

import org.joml.Vector2i;

public abstract class AbstractRegionFileManager<T> implements RegionFileManager<T> {
    private final FileLoadStoreManager<Region<T>> fileManager;

    public AbstractRegionFileManager(String worldName) {
        fileManager = new FileLoadStoreManager<>("worlds/" + worldName, "quadtree", this::regionFromBytes, this::regionToBytes);
    }

    @Override
    public boolean hasFile(Vector2i key) {
        return true;
    }

    public Region<T> loadFile(Vector2i chunkCoord) {
        return fileManager.loadFile(chunkCoord);
    }

    public void saveFile(Vector2i pos, Region<T> region) {
        fileManager.saveFile(region, region.coord);
    }


    protected abstract Region<T> regionFromBytes(byte[] bytes, Vector2i pos);

    protected abstract byte[] regionToBytes(Region<T> region);
}
