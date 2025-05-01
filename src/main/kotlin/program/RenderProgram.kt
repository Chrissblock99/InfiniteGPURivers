package me.chriss99.program

import me.chriss99.glabstractions.VAO

abstract class RenderProgram<T : VAO> : GLProgram() {
    abstract fun render(vao: Collection<T>)
}