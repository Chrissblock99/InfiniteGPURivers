package me.chriss99.glabstractions;

import static org.lwjgl.opengl.GL30.*;

public class VAOImpl implements VAO {
    private final int vao;
    private final VBO<?>[] vbos;
    private final IntGLBuffer ibo;
    private int indexLength;

    public VAOImpl(int[] index, int vertexSize, double[]... vbos) {
        vao = glGenVertexArrays();
        bind();

        this.vbos = new VBO[vbos.length];
        for (int i = 0; i < vbos.length; i++) {
            VBO<?> vbo = new VBO<>(new DoubleGLBuffer(GL_ARRAY_BUFFER).updateData(vbos[i], GL_STATIC_DRAW), vertexSize);

            glVertexAttribPointer(i, vbo.vertexSize, GL_DOUBLE, false, 0, 0);
            glEnableVertexAttribArray(i);

            this.vbos[i] = vbo;
        }

        ibo = (index != null) ? new IntGLBuffer(GL_ELEMENT_ARRAY_BUFFER).updateData(index, GL_STATIC_DRAW) : null;
        indexLength = (index != null) ? index.length : vbos[0].length/vertexSize;
    }

    public VAOImpl(int[] index, int vertexSize, float[]... vbos) {
        vao = glGenVertexArrays();
        bind();

        this.vbos = new VBO[vbos.length];
        for (int i = 0; i < vbos.length; i++) {
            VBO<?> vbo = new VBO<>(new FloatGLBuffer(GL_ARRAY_BUFFER).updateData(vbos[i], GL_STATIC_DRAW), vertexSize);

            glVertexAttribPointer(i, vbo.vertexSize, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(i);

            this.vbos[i] = vbo;
        }

        ibo = (index != null) ? new IntGLBuffer(GL_ELEMENT_ARRAY_BUFFER).updateData(index, GL_STATIC_DRAW) : null;
        indexLength = (index != null) ? index.length : vbos[0].length/vertexSize;
    }

    public void updateVertices(int index, double[] vertices) {
        ((DoubleGLBuffer) vbos[index].buffer).updateData(vertices, GL_STREAM_DRAW);
    }

    public void updateVertices(int index, float[] vertices) {
        ((FloatGLBuffer) vbos[index].buffer).updateData(vertices, GL_STREAM_DRAW);
    }

    public void updateIndices(int[] indices) {
        ibo.updateData(indices, GL_STREAM_DRAW);
        indexLength = indices.length;
    }

    @Override
    public int getIndexLength() {
        return indexLength;
    }

    @Override
    public void bind() {
        glBindVertexArray(vao);
    }

    @Override
    public void delete() {
        for (VBO vbo : vbos)
            vbo.delete();
        if (ibo != null)
            ibo.delete();
        glDeleteVertexArrays(vao);
    }
}
