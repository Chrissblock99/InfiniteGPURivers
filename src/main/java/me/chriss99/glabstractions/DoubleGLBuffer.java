package me.chriss99.glabstractions;

import static org.lwjgl.opengl.GL11.GL_DOUBLE;
import static org.lwjgl.opengl.GL15.glBufferData;

public class DoubleGLBuffer extends GLBuffer {
    public DoubleGLBuffer(int target) {
        super(target, GL_DOUBLE);
    }

    public DoubleGLBuffer updateData(double[] data, int usage) {
        bind();
        glBufferData(target, data, usage);
        return this;
    }
}
