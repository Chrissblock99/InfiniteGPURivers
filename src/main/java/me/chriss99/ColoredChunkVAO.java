package me.chriss99;

import org.joml.Vector2i;

public class ColoredChunkVAO implements ChunkVAO {
    private final ColoredVAO vao;
    private final Vector2i srcPos;
    private final int width;

    public ColoredChunkVAO(ColoredVAO vao, Vector2i srcPos, int width) {
        this.vao = vao;
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

    @Override
    public int getIndexLength() {
        return vao.getIndexLength();
    }

    @Override
    public void bind() {
        vao.bind();
    }

    @Override
    public void delete() {
        vao.delete();
    }
}
