package me.chriss99;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_PATCHES;

public class TerrainVAOListProgram extends RenderProgram {
    private final CameraMatrix cameraMatrix;
    private final int transformMatrix;
    private final int cameraPos;

    private final List<TerrainVAO> vaoList;

    public TerrainVAOListProgram(CameraMatrix cameraMatrix, List<TerrainVAO> vaoList) {
        this.cameraMatrix = cameraMatrix;
        this.vaoList = vaoList;

        addShader("terrain/passThrough.vert", GL_VERTEX_SHADER);
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER);
        addShader("tesselation/different.frag", GL_FRAGMENT_SHADER);

        bindAttribute(0, "position");

        validate();

        transformMatrix = getUniform("transformMatrix");
        cameraPos = getUniform("cameraPos");
    }

    @Override
    public void render() {
        if (vaoList.isEmpty())
            return;

        use();
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix().get(new float[16]));
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z);

        for (TerrainVAO vao : vaoList) {
            vao.bind();

            glDrawElements(GL_TRIANGLES, vao.indexLength(), GL_UNSIGNED_INT, 0);
        }
    }

    @Override
    public void delete() {
        for (TerrainVAO vao : vaoList)
            vao.delete();
        super.delete();
    }
}
