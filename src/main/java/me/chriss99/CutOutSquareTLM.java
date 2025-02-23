package me.chriss99;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.Collection;

public class CutOutSquareTLM<T> extends SquareTLM<T> {
    protected Vector2i skipSrcPos;
    protected Vector2i skipSideLength;

    public CutOutSquareTLM(int renderDistance, Vector2f initPos, Vector2i skipSrcPos, Vector2i skipSideLength) {
        super(renderDistance, initPos);
        this.skipSrcPos = skipSrcPos;
        this.skipSideLength = skipSideLength;
    }

    @Override
    public boolean loadPolicy(Vector2i tilePos, T tile) {
        return super.loadPolicy(tilePos, tile) && !pointInsideRectangle(tilePos, skipSrcPos, skipSideLength);
    }

    @Override
    public Collection<Vector2i> loadCommander() {
        Collection<Vector2i> toLoad = super.loadCommander();
        toLoad.removeIf(v -> pointInsideRectangle(v, skipSrcPos, skipSideLength));
        return toLoad;
    }

    public void setSkipSrcPos(Vector2i skipSrcPos) {
        this.skipSrcPos = new Vector2i(skipSrcPos);
    }

    public void setSkipSideLength(Vector2i skipSideLength) {
        this.skipSideLength = new Vector2i(skipSideLength);
    }


    private static boolean pointInsideRectangle(Vector2i point, Vector2i srcPos, Vector2i sideLength) {
        return (point.x >= srcPos.x && point.x < srcPos.x+sideLength.x) && (point.y >= srcPos.y && point.y < srcPos.y+sideLength.y);
    }
}
