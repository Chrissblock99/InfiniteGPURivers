package me.chriss99.program;

import me.chriss99.glabstractions.VAO;

import java.util.List;

public class ListRenderer<T extends VAO> {
    protected final RenderProgram<T> renderProgram;
    private final List<T> vaoList;

    public ListRenderer(RenderProgram<T> renderProgram, List<T> vaoList) {
        this.renderProgram = renderProgram;
        this.vaoList = vaoList;
    }

    public void render() {
        renderProgram.render(vaoList);
    }

    public void delete() {
        for (T vao : vaoList)
            vao.delete();
        renderProgram.delete();
    }
}
