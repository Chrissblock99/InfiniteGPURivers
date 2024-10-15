package me.chriss99;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void getDirection() {
        byte b = 0;
        assertEquals(new Vector2i(0, -1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(1, 0), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(-1, 0), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(0, 1), new IterationSurfaceType(b).getDirection());

        b++;
        assertEquals(new Vector2i(0, -1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(1, 0), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(-1, 0), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(0, 1), new IterationSurfaceType(b).getDirection());

        b++;
        assertEquals(new Vector2i(-1, -1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(1, -1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(-1, 1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(1, 1), new IterationSurfaceType(b).getDirection());

        b++;
        assertEquals(new Vector2i(-1, -1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(1, -1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(-1, 1), new IterationSurfaceType(b).getDirection());
        b++;
        assertEquals(new Vector2i(1, 1), new IterationSurfaceType(b).getDirection());
    }

    @Test
    void toBits() {
        byte b = 0;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());

        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());

        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());

        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
        b++;
        assertEquals(b, new IterationSurfaceType(b).toBits());
    }
}