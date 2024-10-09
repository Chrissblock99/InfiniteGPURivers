package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.TerrainVAO;
import org.joml.Vector2i;

import java.util.LinkedHashMap;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class TerrainVAOMapProgram extends TerrainRenderer {
    protected final CameraMatrix cameraMatrix;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;
    private final int widthUniform;

    protected final LinkedHashMap<Vector2i, TerrainVAO> terrainVAOs = new LinkedHashMap<>();

    public TerrainVAOMapProgram(CameraMatrix cameraMatrix) {
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
    public void renderTerrain() {
        if (terrainVAOs.isEmpty())
            return;

        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);

        glUniform1i(waterUniform, 0);
        renderAll();
    }

    @Override
    public void renderWater() {
        if (terrainVAOs.isEmpty())
            return;

        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);

        glUniform1i(waterUniform, 1);
        renderAll();
    }

    private void renderAll() {
        for (TerrainVAO vao : terrainVAOs.values()) {
            vao.bind();
            glUniform2i(srcPosUniform, vao.srcPos.x, vao.srcPos.y);
            glUniform1i(widthUniform, vao.width);

            glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
        }
    }

    @Override
    public void delete() {
        for (TerrainVAO vao : terrainVAOs.values())
            vao.delete();
        super.delete();
    }
}
