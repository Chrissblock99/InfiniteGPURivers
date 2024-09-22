package me.chriss99;

import org.joml.Vector2i;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

public class TerrainVAO {
    private final int vao;
    private final int coordVBO;
    private final int indicesVBO;
    private final int indexLength;

    public final Vector2i srcPos;
    public final int width;

    public TerrainVAO(float[] triangle, int[] index, Vector2i srcPos, int width) {
        this.srcPos = srcPos;
        this.width = width;

        this.vao = glGenVertexArrays();
        glBindVertexArray(vao);

        ByteBuffer vertices = Util.storeArrayInBuffer(triangle);
        ByteBuffer indices = Util.storeArrayInBuffer(index);

        coordVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, coordVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        indicesVBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        indexLength = index.length;
    }

    public void updatePositions(float[] positions) {
        glBindBuffer(GL_ARRAY_BUFFER, coordVBO);
        ByteBuffer positionBuffer = Util.storeArrayInBuffer(positions);
        glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STREAM_DRAW);
    }

    public void bind() {
        glBindVertexArray(vao);
    }

    public int indexLength() {
        return indexLength;
    }

    public void delete() {
        glDeleteBuffers(coordVBO);
        glDeleteBuffers(indicesVBO);
        glDeleteVertexArrays(vao);
    }
}
