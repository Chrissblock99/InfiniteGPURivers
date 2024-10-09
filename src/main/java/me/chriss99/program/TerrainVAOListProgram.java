package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.TerrainVAO;

import java.util.LinkedList;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class TerrainVAOListProgram extends RenderProgram {
    private final CameraMatrix cameraMatrix;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;
    private final int widthUniform;

    public final LinkedList<TerrainVAO> terrainVAOs = new LinkedList<>();

    public TerrainVAOListProgram(CameraMatrix cameraMatrix) {
        this.cameraMatrix = cameraMatrix;

        addShader("terrain/passThrough.vert", GL_VERTEX_SHADER);
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER);
        addShader("tesselation/different.frag", GL_FRAGMENT_SHADER);

        bindAttribute(0, "position");

        validate();

        transformMatrix = getUniform("transformMatrix");
        cameraPos = getUniform("cameraPos");
        waterUniform = getUniform("water");
        srcPosUniform = getUniform("srcPos");
        widthUniform = getUniform("width");
    }

    @Override
    public void render() {
        render(false);
    }

    public void render(boolean renderWater) {
        if (terrainVAOs.isEmpty())
            return;

        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);

        glUniform1i(waterUniform, renderWater ? 1 : 0);
        for (TerrainVAO vao : terrainVAOs) {
            vao.bind();
            glUniform2i(srcPosUniform, vao.srcPos.x, vao.srcPos.y);
            glUniform1i(widthUniform, vao.width);

            glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
        }
    }

    @Override
    public void delete() {
        for (TerrainVAO vao : terrainVAOs)
            vao.delete();
        super.delete();
    }
}
