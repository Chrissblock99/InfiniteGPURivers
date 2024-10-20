package me.chriss99.program;

import me.chriss99.TerrainVAO;

import java.util.Collection;

public abstract class TerrainRenderer extends RenderProgram<TerrainVAO> {
    @Override
    public void render(Collection<TerrainVAO> vaos) {
        renderTerrain(vaos);
        renderWater(vaos);
    }

    public abstract void renderTerrain(Collection<TerrainVAO> vao);
    public abstract void renderWater(Collection<TerrainVAO> vao);
}
