package me.chriss99.glabstractions;

public class VBO<T extends GLBuffer> {
    public final T buffer;
    public final int vertexSize;

    public VBO(T buffer, int vertexSize) {
        this.buffer = buffer;
        this.vertexSize = vertexSize;
    }

    public void delete() {
        buffer.delete();
    }
}
