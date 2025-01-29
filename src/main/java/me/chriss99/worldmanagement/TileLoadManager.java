package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.util.Collection;

public interface TileLoadManager<T> {
    boolean loadPolicy(Vector2i tilePos, T tile);
    Collection<Vector2i> loadCommander();
}
