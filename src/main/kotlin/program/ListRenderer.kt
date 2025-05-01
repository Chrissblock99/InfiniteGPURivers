package me.chriss99.program

import me.chriss99.glabstractions.VAO

class ListRenderer<T : VAO>(protected val renderProgram: RenderProgram<T>, private val vaoList: List<T>) {
    fun render() {
        renderProgram.render(vaoList)
    }

    fun delete() {
        for (vao in vaoList) vao!!.delete()
        renderProgram.delete()
    }
}