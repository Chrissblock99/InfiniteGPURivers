package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.VAO;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class VAOListProgram extends RenderProgram {
    private final CameraMatrix cameraMatrix;
    private final int transformMatrix;

    private final List<VAO> vaoList;

    public VAOListProgram(CameraMatrix cameraMatrix, List<VAO> vaoList) {
        this.cameraMatrix = cameraMatrix;
        this.vaoList = vaoList;

        addShader("shader.vert", GL_VERTEX_SHADER);
        addShader("shader.frag", GL_FRAGMENT_SHADER);

        bindAttribute(0, "position");
        bindAttribute(1, "color");

        validate();

        transformMatrix = getUniform("transformMatrix");
    }

    @Override
    public void render() {
        if (vaoList.isEmpty())
            return;

        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

        for (VAO vao : vaoList) {
            vao.bind();

            //draw the current bound VAO/VBO using an index buffer
            glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
        }
    }

    @Override
    public void delete() {
        for (VAO vao : vaoList)
            vao.delete();
        super.delete();
    }
}
