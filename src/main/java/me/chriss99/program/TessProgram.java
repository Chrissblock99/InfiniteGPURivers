package me.chriss99.program;

import me.chriss99.CameraMatrix;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL40.*;

public class TessProgram extends GLProgram {
    private final CameraMatrix cameraMatrix;

    private final int vao;
    private final Vector2i srcPos;
    private final int xSize;
    private final int zSize;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;

    public TessProgram(CameraMatrix cameraMatrix, int vao, Vector2i srcPos, int xSize, int zSize) {
        this.cameraMatrix = cameraMatrix;
        this.vao = vao;
        this.srcPos = srcPos;
        this.xSize = xSize;
        this.zSize = zSize;

        addShader("tesselation/passThrough.vert", GL_VERTEX_SHADER);
        addShader("tesselation/tess.tesc", GL_TESS_CONTROL_SHADER);
        addShader("tesselation/tess.tese", GL_TESS_EVALUATION_SHADER);
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER);
        addShader("tesselation/different.frag", GL_FRAGMENT_SHADER);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        bindAttribute(0, "position");
        validate();

        transformMatrix = getUniform("transformMatrix");
        cameraPos = getUniform("cameraPos");
        waterUniform = getUniform("water");
        srcPosUniform = getUniform("srcPos");
    }

    public void renderTerrain() {
        use();
        glUniform2i(srcPosUniform, srcPos.x, srcPos.y);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        glBindVertexArray(vao);

        glUniform1i(waterUniform, 0);
        glDrawArrays(GL_PATCHES, 0, xSize/64*zSize/64*4);
    }

    public void renderWater() {
        use();
        glUniform2i(srcPosUniform, srcPos.x, srcPos.y);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        glBindVertexArray(vao);

        glUniform1i(waterUniform, 1);
        glDrawArrays(GL_PATCHES, 0, xSize/64*zSize/64*4);

    }
}
