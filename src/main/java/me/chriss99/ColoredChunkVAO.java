package me.chriss99;

import org.joml.Vector2i;

public class ColoredChunkVAO extends ColoredVAO implements ChunkVAO {
    private final Vector2i srcPos;
    private final int width;

    public ColoredChunkVAO(double[] triangle, double[] color, int[] index, Vector2i srcPos, int width) {
        super(triangle, color, index);
        this.srcPos = srcPos;
        this.width = width;
    }

    @Override
    public Vector2i getSrcPos() {
        return srcPos;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
