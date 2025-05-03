package me.chriss99.program

import me.chriss99.glabstractions.VAO

open class ListRenderer<T : VAO>(protected val renderProgram: RenderProgram<T>, protected val vaoList: List<T>) {
    fun render() = renderProgram.render(vaoList)

    fun delete() {
        vaoList.forEach { it.delete() }
        renderProgram.delete()
    }
}