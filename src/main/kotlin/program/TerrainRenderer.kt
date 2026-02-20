package me.chriss99.program

import me.chriss99.render.TerrainVAO

abstract class TerrainRenderer(folder: String = "", vararg fileNames: String) : RenderProgram<TerrainVAO>(folder, * fileNames) {
    override fun render(vaos: Collection<TerrainVAO>) {
        renderTerrain(vaos)
        renderWater(vaos)
    }

    abstract fun renderTerrain(vao: Collection<TerrainVAO>)
    abstract fun renderWater(vao: Collection<TerrainVAO>)
}