package me.chriss99.program;

import me.chriss99.Area;
import me.chriss99.CameraMatrix;
import me.chriss99.ColoredVAOGenerator;
import me.chriss99.glabstractions.VAO;
import me.chriss99.glabstractions.VAOImpl;
import org.joml.Vector2i;

import static org.lwjgl.opengl.GL40.*;

public class TessProgram extends GLProgram {
    private final CameraMatrix cameraMatrix;

    private final VAO vao;
    private Area area;
    private final Vector2i maxSize;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;

    public TessProgram(CameraMatrix cameraMatrix, Area area) {
        this.cameraMatrix = cameraMatrix;
        setArea(area);
        maxSize = this.area.getSize();
        this.vao = new VAOImpl(null, 2, ColoredVAOGenerator.tesselationGridVertexesTest(maxSize.x, maxSize.y, 64));

        addShader("tesselation/passThrough.vert", GL_VERTEX_SHADER);
        addShader("tesselation/tess.tesc", GL_TESS_CONTROL_SHADER);
        addShader("tesselation/tess.tese", GL_TESS_EVALUATION_SHADER);
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER);
        addShader("tesselation/different.frag", GL_FRAGMENT_SHADER);

        glPatchParameteri(GL_PATCH_VERTICES, 4);
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
        render(false);
    }

    public void renderWater() {
        render(true);
    }

    private void render(boolean water) {
        use();
        glUniform2i(srcPosUniform, area.srcPos().x*64, area.srcPos().y*64);
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);
        vao.bind();

        glUniform1i(waterUniform, water ? 1 : 0);

        Vector2i size = area.getSize();
        for (int y = 0; y < size.y; y++)
            glDrawArrays(GL_PATCHES, maxSize.x*y*4, size.x*4);
    }

    public void setArea(Area area) {
        this.area = area.div(64);
    }

    @Override
    public void delete() {
        vao.delete();
        super.delete();
    }
}
