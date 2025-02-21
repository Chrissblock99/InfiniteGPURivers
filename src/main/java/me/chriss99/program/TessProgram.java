package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.ColoredVAOGenerator;
import me.chriss99.glabstractions.VAO;
import me.chriss99.glabstractions.VAOImpl;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL40.*;

public class TessProgram extends GLProgram {
    private final CameraMatrix cameraMatrix;

    private final VAO vao;
    private Vector2i srcPos;
    private final int xSize;
    private final int zSize;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;

    public TessProgram(CameraMatrix cameraMatrix, Vector2i srcPos, int xSize, int zSize) {
        this.cameraMatrix = cameraMatrix;
        this.vao = new VAOImpl(null, 2, ColoredVAOGenerator.tesselationGridVertexesTest(xSize/64, zSize/64, 64));
        setSrcPos(srcPos);
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
        glPatchParameteri(GL_PATCH_VERTICES, 4);
        use();
        glUniform2i(srcPosUniform, srcPos.x, srcPos.y);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        vao.bind();

        glUniform1i(waterUniform, 0);
        glDrawArrays(GL_PATCHES, 0, xSize/64*zSize/64*4);
    }

    public void renderWater() {
        glPatchParameteri(GL_PATCH_VERTICES, 4);
        use();
        glUniform2i(srcPosUniform, srcPos.x, srcPos.y);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        vao.bind();

        glUniform1i(waterUniform, 1);
        glDrawArrays(GL_PATCHES, 0, xSize/64*zSize/64*4);
    }

    public void setSrcPos(Vector2i srcPos) {
        this.srcPos = new Vector2i(srcPos);
    }

    @Override
    public void delete() {
        vao.delete();
        super.delete();
    }
}
