package me.chriss99.program

import me.chriss99.Area
import me.chriss99.CameraMatrix
import me.chriss99.ColoredVAOGenerator.tesselationGridVertexesTest
import me.chriss99.glabstractions.VAO
import me.chriss99.glabstractions.VAOImpl
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL40.*
import java.nio.FloatBuffer

class TessProgram(private val cameraMatrix: CameraMatrix, area: Area) : GLProgram() {
    private val vao: VAO
    private var area: Area? = null
    private val maxSize: Vec2i

    private val transformMatrix: Int
    private val cameraPos: Int
    private val waterUniform: Int
    private val srcPosUniform: Int

    init {
        setArea(area)
        maxSize = this.area!!.size
        this.vao = VAOImpl(null, 2, tesselationGridVertexesTest(maxSize.x, maxSize.y, 64.0))

        addShader("tesselation/passThrough.vert", GL_VERTEX_SHADER)
        addShader("tesselation/tess.tesc", GL_TESS_CONTROL_SHADER)
        addShader("tesselation/tess.tese", GL_TESS_EVALUATION_SHADER)
        addShader("tesselation/normals.geom", GL_GEOMETRY_SHADER)
        addShader("tesselation/different.frag", GL_FRAGMENT_SHADER)

        glPatchParameteri(GL_PATCH_VERTICES, 4)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        bindAttribute(0, "position")
        validate()

        transformMatrix = getUniform("transformMatrix")
        cameraPos = getUniform("cameraPos")
        waterUniform = getUniform("water")
        srcPosUniform = getUniform("srcPos")
    }

    fun renderTerrain() {
        render(false)
    }

    fun renderWater() {
        render(true)
    }

    private fun render(water: Boolean) {
        use()
        glUniform2i(srcPosUniform, area!!.srcPos().x * 64, area!!.srcPos().y * 64)
        glUniformMatrix4fv(transformMatrix, false, (cameraMatrix.generateMatrix() to FloatBuffer.allocate(16)).array())
        glUniform3f(cameraPos, cameraMatrix.position.x, cameraMatrix.position.y, cameraMatrix.position.z)
        vao.bind()

        glUniform1i(waterUniform, if (water) 1 else 0)

        val size: Vec2i = area!!.size
        for (y in 0..<size.y)
            glDrawArrays(GL_PATCHES, maxSize.x * y * 4, size.x * 4)
    }

    fun setArea(area: Area) {
        this.area = area.div(64)
    }

    override fun delete() {
        vao.delete()
        super.delete()
    }
}