package me.chriss99.program

import me.chriss99.CameraMatrix
import me.chriss99.ColoredVAO
import org.lwjgl.opengl.GL20.*
import java.nio.FloatBuffer

class ColoredVAORenderer(private val cameraMatrix: CameraMatrix) : RenderProgram<ColoredVAO>() {
    private val transformMatrix: Int

    init {
        addShader("shader.vert", GL_VERTEX_SHADER)
        addShader("shader.frag", GL_FRAGMENT_SHADER)

        bindAttribute(0, "position")
        bindAttribute(1, "color")

        validate()

        transformMatrix = getUniform("transformMatrix")
    }

    override fun render(vaos: Collection<ColoredVAO>) {
        if (vaos.isEmpty()) return

        use()
        glUniformMatrix4fv(transformMatrix, false, cameraMatrix.generateMatrix() to FloatBuffer.allocate(16))

        for (vao in vaos) {
            vao.bind()

            glDrawElements(GL_TRIANGLES, vao.indexLength, GL_UNSIGNED_INT, 0)
        }
    }
}