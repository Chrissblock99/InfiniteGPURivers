package me.chriss99.worldmanagement.quadtree;

import me.chriss99.IterationSurfaceType;
import org.joml.Vector2i;

import java.util.Objects;

public class IterationSurface {
    private final Quad<IterationSurfaceType> quad;
    private int iteration;

    public IterationSurface(Vector2i pos, int size) {
        this(0, new Quad<>(new IterationSurfaceType((byte) 0b0000), pos, size));
    }

    protected IterationSurface(int iteration, Quad<IterationSurfaceType> quad) {
        this.quad = quad;
        this.iteration = iteration;
    }

    public Quad<IterationSurfaceType> getQuad() {
        return quad;
    }

    public int getIteration() {
        return iteration;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IterationSurface that)) return false;
        return iteration == that.iteration && Objects.equals(quad, that.quad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quad, iteration);
    }
}
