package me.chriss99.program

import me.chriss99.CameraMatrix
import me.chriss99.TerrainVAO
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER
import java.nio.FloatBuffer

class TerrainVAORenderer(protected val cameraMatrix: CameraMatrix) : TerrainRenderer() {
    private val transformMatrix: Int
    private val cameraPos: Int
    private val waterUniform: Int
    private val srcPosUniform: Int
    private val widthUniform: Int
    private val scaleUniform: Int

    init {
        addShader("terrain/passThrough.vert", GL_VERTEX_SHADER)
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER)
        addShader("tesselation/colors.frag", GL_FRAGMENT_SHADER)

        bindAttribute(0, "position")

        validate()

        transformMatrix = getUniform("transformMatrix")
        cameraPos = getUniform("cameraPos")
        waterUniform = getUniform("water")
        srcPosUniform = getUniform("srcPos")
        widthUniform = getUniform("width")
        scaleUniform = getUniform("scale")
    }

    override fun renderTerrain(vaos: Collection<TerrainVAO>) {
        use()
        glUniform1i(waterUniform, 0)
        renderAll(vaos)
    }

    override fun renderWater(vaos: Collection<TerrainVAO>) {
        use()
        glUniform1i(waterUniform, 1)
        renderAll(vaos)
    }

    private fun renderAll(vaos: Collection<TerrainVAO>) {
        glUniformMatrix4fv(transformMatrix, false, (cameraMatrix.generateMatrix() to FloatBuffer.allocate(16)).array())
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z)

        for (vao in vaos) {
            vao.bind()
            glUniform2i(srcPosUniform, vao.srcPos.x, vao.srcPos.y)
            glUniform1i(widthUniform, vao.width)
            glUniform1i(scaleUniform, vao.scale)

            glDrawElements(GL_TRIANGLES, vao.indexLength, GL_UNSIGNED_INT, 0)
        }
    }
}