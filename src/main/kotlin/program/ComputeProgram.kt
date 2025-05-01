package me.chriss99.program

import org.lwjgl.opengl.GL45.*

open class ComputeProgram(name: String) : GLProgram() {
    init {
        addShader("compute/$name.comp", GL_COMPUTE_SHADER)
        validate()
    }
}