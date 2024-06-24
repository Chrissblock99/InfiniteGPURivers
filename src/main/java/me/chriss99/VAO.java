package me.chriss99;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

public class VAO {
    private final int vao;
    private final int coordVBO;
    private final int colorVBO;
    private final int indicesVBO;
    private final int indexLength;

    public VAO(double[] triangle, double[] color, int[] index) {
        this.vao = glGenVertexArrays();
        glBindVertexArray(vao);

        ByteBuffer vertices = Main.storeArrayInBuffer(triangle);
        ByteBuffer colors = Main.storeArrayInBuffer(color);
        ByteBuffer indices = Main.storeArrayInBuffer(index);

        coordVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, coordVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_DOUBLE, false, 0, 0);
        glEnableVertexAttribArray(0);

        colorVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorVBO);
        glBufferData(GL_ARRAY_BUFFER, colors, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_DOUBLE, false, 0, 0);
        glEnableVertexAttribArray(1);

        indicesVBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        indexLength = index.length;
    }

    public void bind() {
        glBindVertexArray(vao);
    }

    public int indexLength() {
        return indexLength;
    }

    public void delete() {
        glDeleteBuffers(coordVBO);
        glDeleteBuffers(colorVBO);
        glDeleteBuffers(indicesVBO);
        glDeleteVertexArrays(vao);
    }
}
