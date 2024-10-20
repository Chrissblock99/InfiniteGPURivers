package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.ColoredVAO;

import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class ColoredVAORenderer extends RenderProgram<ColoredVAO> {
    private final CameraMatrix cameraMatrix;
    private final int transformMatrix;

    public ColoredVAORenderer(CameraMatrix cameraMatrix) {
        this.cameraMatrix = cameraMatrix;

        addShader("shader.vert", GL_VERTEX_SHADER);
        addShader("shader.frag", GL_FRAGMENT_SHADER);

        bindAttribute(0, "position");
        bindAttribute(1, "color");

        validate();

        transformMatrix = getUniform("transformMatrix");
    }

    @Override
    public void render(Collection<ColoredVAO> vaos) {
        if (vaos.isEmpty())
            return;

        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));

        for (ColoredVAO vao : vaos) {
            vao.bind();

            glDrawElements(GL_TRIANGLES, vao.getIndexLength(), GL_UNSIGNED_INT, 0);
        }
    }
}
