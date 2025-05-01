package me.chriss99;

import me.chriss99.worldmanagement.TileLoadManager;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.List;

public class LeakingTLM<T> implements TileLoadManager<T> {
    public LeakingTLM() {
        System.err.println("""
                -------------------------------------
                
                This is (probably) leaking memory!
                If you see this someone was too lazy
                to implement proper load management
                of some tile based system.
                Restarting often enough might
                prevent crashes.
                
                -------------------------------------""");
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
