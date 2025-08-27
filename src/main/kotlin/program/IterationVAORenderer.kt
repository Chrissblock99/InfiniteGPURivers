package me.chriss99.program

import me.chriss99.CameraMatrix
import me.chriss99.render.IterationVAO
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER
import java.nio.FloatBuffer

class IterationVAORenderer(protected val cameraMatrix: CameraMatrix) : RenderProgram<IterationVAO>() {
    private val transformMatrix: Int
    private val cameraPos: Int
    private val srcPosUniform: Int
    private val widthUniform: Int

    init {
        addShader("iteration/passThrough.vert", GL_VERTEX_SHADER)
        addShader("iteration/normals.geom", GL_GEOMETRY_SHADER)
        addShader("iteration/lighting.frag", GL_FRAGMENT_SHADER)

        bindAttribute(0, "position")

        validate()

        transformMatrix = getUniform("transformMatrix")
        cameraPos = getUniform("cameraPos")
        srcPosUniform = getUniform("srcPos")
        widthUniform = getUniform("width")
    }

    override fun render(vaos: Collection<IterationVAO>) {
        use()
        glUniformMatrix4fv(transformMatrix, false, (cameraMatrix.generateMatrix() to FloatBuffer.allocate(16)).array())
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z)

        for (vao in vaos) {
            vao.bind()
            glUniform2i(srcPosUniform, vao.srcPos.x, vao.srcPos.y)
            glUniform1i(widthUniform, vao.width)

            glDrawArrays(GL_TRIANGLES, 0, vao.indexLength)
        }
    }
}