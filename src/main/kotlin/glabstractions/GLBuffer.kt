package me.chriss99.glabstractions;

import static org.lwjgl.opengl.GL15.*;

public class GLBuffer implements GLObject {
    private final int buffer;
    public final int target;
    public final int type;

    public GLBuffer(int target, int type) {
        buffer = glGenBuffers();
        this.target = target;
        this.type = type;
    }

    @Override
    public void bind() {
        glBindBuffer(target, buffer);
    }

    @Override
    public void delete() {
        glDeleteBuffers(buffer);
    }
}
