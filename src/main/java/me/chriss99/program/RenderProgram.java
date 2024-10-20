package me.chriss99.program;

import me.chriss99.glabstractions.VAO;

import java.util.Collection;

public abstract class RenderProgram<T extends VAO> extends GLProgram {
    public abstract void render(Collection<T> vao);
}
