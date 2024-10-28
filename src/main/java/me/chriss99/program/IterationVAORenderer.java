package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.IterationVAO;
import me.chriss99.TerrainVAO;

import java.util.Collection;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class IterationVAORenderer extends RenderProgram<IterationVAO> {
    protected final CameraMatrix cameraMatrix;

    private final int transformMatrix;
    private final int cameraPos;
    private final int srcPosUniform;
    private final int widthUniform;

    public IterationVAORenderer(CameraMatrix cameraMatrix) {
        this.cameraMatrix = cameraMatrix;

        addShader("iteration/passThrough.vert", GL_VERTEX_SHADER);
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER);
        addShader("iteration/different.frag", GL_FRAGMENT_SHADER);

        bindAttribute(0, "position");

        validate();

        transformMatrix = getUniform("transformMatrix");
        cameraPos = getUniform("cameraPos");
        srcPosUniform = getUniform("srcPos");
        widthUniform = getUniform("width");
    }

    @Override
    public void render(Collection<IterationVAO> vaos) {
        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);

        for (IterationVAO vao : vaos) {
            vao.bind();
            glUniform2i(srcPosUniform, vao.getSrcPos().x, vao.getSrcPos().y);
            glUniform1i(widthUniform, vao.getWidth());

            glDrawArrays(GL_TRIANGLES, 0, vao.getIndexLength());
        }
    }
}
