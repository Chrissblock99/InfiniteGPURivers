package me.chriss99.program

import me.chriss99.TerrainVAO

abstract class TerrainRenderer : RenderProgram<TerrainVAO>() {
    override fun render(vaos: Collection<TerrainVAO>) {
        renderTerrain(vaos)
        renderWater(vaos)
    }

    abstract fun renderTerrain(vao: Collection<TerrainVAO>)
    abstract fun renderWater(vao: Collection<TerrainVAO>)
}