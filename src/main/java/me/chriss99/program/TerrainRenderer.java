package me.chriss99.program;

public abstract class TerrainRenderer extends RenderProgram {
    @Override
    public void render() {
        renderTerrain();
        renderWater();
    }

    public abstract void renderTerrain();
    public abstract void renderWater();
}
