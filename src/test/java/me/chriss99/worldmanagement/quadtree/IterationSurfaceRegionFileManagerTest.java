package me.chriss99.worldmanagement.quadtree;

import me.chriss99.IterationSurfaceType;
import me.chriss99.worldmanagement.Region;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IterationSurfaceRegionFileManagerTest {

    @Test
    public void bijective() {
        int tileSize = 4;
        IterationSurfaceRegionFileManager manager = new IterationSurfaceRegionFileManager("jUnitTest", tileSize);
        Vector2i pos = new Vector2i();

        for (int i = 0; i < 1000; i++) {
            try {
                Region<IterationSurface> quadRegion = randomIterationSurfaceRegion(pos, 10, .95, 0.9, tileSize);

                manager.saveRegion(quadRegion);
                Region<IterationSurface> reconstructed = manager.loadRegion(pos);

                assertEquals(quadRegion, reconstructed);
            } catch (StackOverflowError ignored) {}
        }
    }

    public static Region<IterationSurface> randomIterationSurfaceRegion(Vector2i pos, int length, double chance, double chanceMul, int size) {
        Region<IterationSurface> quadRegion = new Region<>(pos);

        for (int i = 0; i < length; i++) {
            Quad<IterationSurfaceType> quad = randomTree(chance, chanceMul, size);
            quadRegion.addChunk(quad.getPos(), new IterationSurface(randomInt(), quad));
        }

        return quadRegion;
    }

    public static Quad<IterationSurfaceType> randomTree(double chance, double chanceMul, int size) {
        Quad<IterationSurfaceType> quad = new Quad<>(randomType(), randomPos(), size);
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

    public static Vector2i randomPos() {
        return new Vector2i(randomInt(), randomInt());
    }

    public static int randomInt() {
        return (int) (Math.random()*Integer.MAX_VALUE);
    }
}