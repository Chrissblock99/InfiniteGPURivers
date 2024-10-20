package me.chriss99.glabstractions;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.glBufferData;

public class FloatGLBuffer extends GLBuffer {
    public FloatGLBuffer(int target) {
        super(target, GL_FLOAT);
    }

    public FloatGLBuffer updateData(float[] data, int usage) {
        bind();
        glBufferData(target, data, usage);
        return this;
    }
}
