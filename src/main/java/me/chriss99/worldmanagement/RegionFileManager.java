package me.chriss99.worldmanagement;

import org.joml.Vector2i;

public interface RegionFileManager<T> {
    Region<T> loadRegion(Vector2i chunkCoord);
    void saveRegion(Region<T> region);
}
