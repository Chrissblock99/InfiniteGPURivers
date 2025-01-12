package me.chriss99.worldmanagement.quadtree;

import me.chriss99.IterationSurfaceType;
import me.chriss99.worldmanagement.Region;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class QuadRegionFileManagerTest {

    @Test
    public void bijective() {
        int tileSize = 4;
        QuadRegionFileManager manager = new QuadRegionFileManager("jUnitTest", tileSize);
        Vector2i pos = new Vector2i();

        for (int i = 0; i < 1000; i++) {
            try {
                Region<Quad<IterationSurfaceType>> quadRegion = randomTreeRegion(pos, 10, .95, 0.9, tileSize);

                manager.saveRegion(quadRegion);
                Region<Quad<IterationSurfaceType>> reconstructed = manager.loadRegion(pos);

                assertEquals(quadRegion, reconstructed);
            } catch (StackOverflowError ignored) {}
        }
    }

    public static Region<Quad<IterationSurfaceType>> randomTreeRegion(Vector2i pos, int length, double chance, double chanceMul, int size) {
        Region<Quad<IterationSurfaceType>> quadRegion = new Region<>(pos);

        for (int i = 0; i < length; i++) {
            Quad<IterationSurfaceType> quad = randomTree(chance, chanceMul, size);
            quadRegion.addChunk(quad.getPos(), quad);
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
        return new Vector2i((int) (Math.random()*Integer.MAX_VALUE), (int) (Math.random()*Integer.MAX_VALUE));
    }
}