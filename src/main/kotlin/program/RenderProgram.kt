package me.chriss99.program

import me.chriss99.glabstractions.VAO

abstract class RenderProgram<T : VAO>(folder: String = "", vararg fileNames: String) : GLProgram(folder, *fileNames) {
    abstract fun render(vao: Collection<T>)
}