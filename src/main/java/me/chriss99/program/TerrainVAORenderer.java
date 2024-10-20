package me.chriss99.program;

import me.chriss99.CameraMatrix;
import me.chriss99.TerrainVAO;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class TerrainVAORenderer extends TerrainRenderer {
    protected final CameraMatrix cameraMatrix;

    private final int transformMatrix;
    private final int cameraPos;
    private final int waterUniform;
    private final int srcPosUniform;
    private final int widthUniform;

    public TerrainVAORenderer(CameraMatrix cameraMatrix) {
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
    public void renderTerrain(Collection<TerrainVAO> vaos) {
        use();
        glUniform1i(waterUniform, 0);
        renderAll(vaos);
    }

    @Override
    public void renderWater(Collection<TerrainVAO> vaos) {
        use();
        glUniform1i(waterUniform, 1);
        renderAll(vaos);
    }

    private void renderAll(Collection<TerrainVAO> vaos) {
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);

        for (TerrainVAO vao : vaos) {
            vao.bind();
            glUniform2i(srcPosUniform, vao.getSrcPos().x, vao.getSrcPos().y);
            glUniform1i(widthUniform, vao.getWidth());

            glDrawElements(GL_TRIANGLES, vao.getIndexLength(), GL_UNSIGNED_INT, 0);
        }
    }
}
