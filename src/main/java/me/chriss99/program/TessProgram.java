package me.chriss99.program;

import me.chriss99.Area;
import me.chriss99.CameraMatrix;
import me.chriss99.ColoredVAOGenerator;
import me.chriss99.glabstractions.VAO;
import me.chriss99.glabstractions.VAOImpl;

import static org.lwjgl.opengl.GL40.*;

public class TessProgram extends GLProgram {
    private final CameraMatrix cameraMatrix;

    private final VAO vao;
    private Area area;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;

    public TessProgram(CameraMatrix cameraMatrix, Area area) {
        this.cameraMatrix = cameraMatrix;
        this.vao = new VAOImpl(null, 2, ColoredVAOGenerator.tesselationGridVertexesTest(area.getSize().x/64, area.getSize().y/64, 64));
        setArea(area);

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
        glUniform2i(srcPosUniform, area.srcPos().x, area.srcPos().y);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        vao.bind();

        glUniform1i(waterUniform, 0);
        glDrawArrays(GL_PATCHES, 0, area.getSize().x/64*area.getSize().y/64*4);
    }

    public void renderWater() {
        glPatchParameteri(GL_PATCH_VERTICES, 4);
        use();
        glUniform2i(srcPosUniform, area.srcPos().x, area.srcPos().y);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        vao.bind();

        glUniform1i(waterUniform, 1);
        glDrawArrays(GL_PATCHES, 0, area.getSize().x/64*area.getSize().y/64*4);
    }

    public void setArea(Area area) { //TODO this doesn't change the size!
        this.area = area.copy();
    }

    @Override
    public void delete() {
        vao.delete();
        super.delete();
    }
}
