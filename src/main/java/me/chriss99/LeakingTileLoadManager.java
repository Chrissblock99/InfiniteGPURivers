package me.chriss99;

import me.chriss99.worldmanagement.TileLoadManager;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.List;

public class LeakingTileLoadManager<T> implements TileLoadManager<T> {
    public LeakingTileLoadManager() {
        new IllegalStateException("You are leaking memory!").printStackTrace();
    }

    @Override
    public boolean loadPolicy(Vector2i tilePos, T tile) {
        return true;
    }

    @Override
    public Collection<Vector2i> loadCommander() {
        return List.of();
    }
}
