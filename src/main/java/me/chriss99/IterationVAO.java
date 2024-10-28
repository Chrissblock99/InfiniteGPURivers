package me.chriss99;

import me.chriss99.glabstractions.VAOImpl;
import org.joml.Vector2i;

public class IterationVAO implements ChunkVAO {
    private final VAOImpl vao;
    private final Vector2i srcPos;
    private final int width;

    public IterationVAO(float[] triangle, Vector2i srcPos, int width) {
        vao = new VAOImpl(null, 3, triangle);
        this.srcPos = srcPos;
        this.width = width;
    }

    public void updatePositions(float[] positions) {
        vao.updateVertices(0, positions);
    }

    @Override
    public Vector2i getSrcPos() {
        return new Vector2i(srcPos);
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
