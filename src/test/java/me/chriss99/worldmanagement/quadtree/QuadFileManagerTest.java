package me.chriss99.worldmanagement.quadtree;

import me.chriss99.IterationSurfaceType;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuadFileManagerTest {

    @Test
    public void bijective() {
        int tileSize = 4;
        QuadFileManager manager = new QuadFileManager("jUnitTest", tileSize);
        Vector2i pos = new Vector2i();

        for (int i = 0; i < 1000; i++) {
            try {
                Quad<IterationSurfaceType> quad = randomTree(.95, 0.9, pos, tileSize);

                manager.saveQuad(quad);
                Quad<IterationSurfaceType> reconstructed = manager.loadQuad(pos);

                assertEquals(quad, reconstructed);
            } catch (StackOverflowError ignored) {}
        }
    }

    public static Quad<IterationSurfaceType> randomTree(double chance, double chanceMul, Vector2i pos, int size) {
        Quad<IterationSurfaceType> quad = new Quad<>(randomType(), pos, size);
        randomSubdivide(quad, chance, chanceMul);
        return quad;
    }

    private static void randomSubdivide(Quad<IterationSurfaceType> quad, double chance, double chanceMul) {
        if (Math.random() >= chance)
            return;

        chance *= chanceMul;
        if (Math.random() <= chance)
            chance *= chanceMul;
        if (Math.random() <= chance)
            chance *= chanceMul;
        if (Math.random() <= chance)
            chance *= chanceMul;
        if (Math.random() <= chance)
            chance *= chanceMul;

        quad.subdivide(randomType(), randomType(), randomType(), randomType());
        randomSubdivide(quad.getTopLeft(), chance, chanceMul);
        randomSubdivide(quad.getTopRight(), chance, chanceMul);
        randomSubdivide(quad.getBottomLeft(), chance, chanceMul);
        randomSubdivide(quad.getBottomRight(), chance, chanceMul);
    }

    public static IterationSurfaceType randomType() {
        return new IterationSurfaceType((byte) (Math.random()*16));
    }
}