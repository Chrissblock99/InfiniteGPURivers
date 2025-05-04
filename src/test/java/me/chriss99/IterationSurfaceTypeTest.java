package me.chriss99;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

class IterationSurfaceTypeTest {

    @Test
    void getSurfaceType() {
        byte b = 0;
        assertEquals(IterationSurfaceType.SurfaceType.FLAT, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.FLAT, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.FLAT, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.FLAT, new IterationSurfaceType(b).getSurfaceType());

        b++;
        assertEquals(IterationSurfaceType.SurfaceType.SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.SLOPE, new IterationSurfaceType(b).getSurfaceType());

        b++;
        assertEquals(IterationSurfaceType.SurfaceType.OUTWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.OUTWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.OUTWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.OUTWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());

        b++;
        assertEquals(IterationSurfaceType.SurfaceType.INWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.INWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.INWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
        b++;
        assertEquals(IterationSurfaceType.SurfaceType.INWARD_SLOPE, new IterationSurfaceType(b).getSurfaceType());
    }

    @Test
    void direction() {
        byte b = 0;
        assertEquals(new Vector2i(0, -1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(1, 0), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(-1, 0), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(0, 1), new IterationSurfaceType(b).direction);

        b++;
        assertEquals(new Vector2i(0, -1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(1, 0), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(-1, 0), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(0, 1), new IterationSurfaceType(b).direction);

        b++;
        assertEquals(new Vector2i(-1, -1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(1, -1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(-1, 1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(1, 1), new IterationSurfaceType(b).direction);

        b++;
        assertEquals(new Vector2i(-1, -1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(1, -1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(-1, 1), new IterationSurfaceType(b).direction);
        b++;
        assertEquals(new Vector2i(1, 1), new IterationSurfaceType(b).direction);
    }

    @Test
    void bits() {
        byte b = 0;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);

        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);

        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);

        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
        b++;
        assertEquals(b, new IterationSurfaceType(b).bits);
    }

    @Test
    void getSurface() {
        byte b = 0;
        assertArrayEquals(new int[2][2], new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[2][2], new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[2][2], new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[2][2], new IterationSurfaceType(b).getSurface());

        b++;
        assertArrayEquals(new int[][]{{0, 0}, {1, 1}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{1, 0}, {1, 0}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{0, 1}, {0, 1}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{1, 1}, {0, 0}}, new IterationSurfaceType(b).getSurface());

        b++;
        assertArrayEquals(new int[][]{{0, 0}, {0, 1}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{0, 0}, {1, 0}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{0, 1}, {0, 0}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{1, 0}, {0, 0}}, new IterationSurfaceType(b).getSurface());

        b++;
        assertArrayEquals(new int[][]{{0, 1}, {1, 1}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{1, 0}, {1, 1}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{1, 1}, {0, 1}}, new IterationSurfaceType(b).getSurface());
        b++;
        assertArrayEquals(new int[][]{{1, 1}, {1, 0}}, new IterationSurfaceType(b).getSurface());
    }
}