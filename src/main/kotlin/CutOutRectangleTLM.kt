package me.chriss99;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.Collection;

public class CutOutRectangleTLM<T> extends SquareTLM<T> {
    protected Area skipArea;

    public CutOutRectangleTLM(int renderDistance, Vector2f initPos, Area skipArea) {
        super(renderDistance, initPos);
        this.skipArea = skipArea.copy();
    }

    @Override
    public boolean loadPolicy(Vector2i tilePos, T tile) {
        return super.loadPolicy(tilePos, tile) && !skipArea.contains(tilePos);
    }

    @Override
    public Collection<Vector2i> loadCommander() {
        Collection<Vector2i> toLoad = super.loadCommander();
        toLoad.removeIf(v -> skipArea.contains(v));
        return toLoad;
    }

    public void setSkipArea(Area skipArea) {
        this.skipArea = skipArea;
    }
}
