package me.chriss99.glabstractions;

import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.glBufferData;

public class IntGLBuffer extends GLBuffer {
    public IntGLBuffer(int target) {
        super(target, GL_INT);
    }

    public IntGLBuffer updateData(int[] data, int usage) {
        bind();
        glBufferData(target, data, usage);
        return this;
    }
}
