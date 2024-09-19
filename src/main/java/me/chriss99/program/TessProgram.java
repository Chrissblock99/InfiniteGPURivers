package me.chriss99.program;

import me.chriss99.CameraMatrix;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.*;

public class TessProgram extends RenderProgram {
    private final CameraMatrix cameraMatrix;

    private final int vao;
    private final int xSize;
    private final int zSize;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;

    public TessProgram(CameraMatrix cameraMatrix, int vao, int xSize, int zSize, boolean niceLooking) {
        this.cameraMatrix = cameraMatrix;
        this.vao = vao;
        this.xSize = xSize;
        this.zSize = zSize;

        addShader("tesselation/passThrough.vert", GL_VERTEX_SHADER);
        addShader("tesselation/tess.tesc", GL_TESS_CONTROL_SHADER);
        if (!niceLooking) {
            addShader("tesselation/tess.tese", GL_TESS_EVALUATION_SHADER);
            addShader("tesselation/gradient.frag", GL_FRAGMENT_SHADER);
        } else {
            addShader("tesselation/niceTess.tese", GL_TESS_EVALUATION_SHADER);
            addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER);
            addShader("tesselation/different.frag", GL_FRAGMENT_SHADER);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        bindAttribute(0, "position");
        validate();

        transformMatrix = getUniform("transformMatrix");
        cameraPos = getUniform("cameraPos");
        waterUniform = getUniform("water");
    }

    @Override
    public void render() {
        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        glBindVertexArray(vao);

        glUniform1i(waterUniform, 0);
        glDrawArrays(GL_PATCHES, 0, xSize/64*zSize/64*4);
        glUniform1i(waterUniform, 1);
        glDrawArrays(GL_PATCHES, 0, xSize/64*zSize/64*4);
    }
}
