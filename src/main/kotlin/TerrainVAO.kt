package me.chriss99;

import me.chriss99.glabstractions.VAOImpl;
import org.joml.Vector2i;

public class TerrainVAO implements ChunkVAO {
    private final VAOImpl vao;
    private final Vector2i srcPos;
    private final int width;
    private final int scale;

    public TerrainVAO(float[] triangle, int[] index, Vector2i srcPos, int width, int scale) {
        vao = new VAOImpl(index, 2, triangle);
        this.srcPos = srcPos;
        this.width = width;
        this.scale = scale;
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

    public int getScale() {
        return scale;
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
